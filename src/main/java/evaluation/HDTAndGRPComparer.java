package evaluation;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HDTAndGRPComparer {

    private static final String DIRECTORY = "/Users/philipfrerk/Documents/RDF_data/Comparison/opendata";

    private static final boolean DIR_OF_DIRS = false;


    private static List<File> filesToRemove = new ArrayList<>();

    private static final long BOUND_SUBGRAPH = 10000000;

    private static final int SUB_GRAPH_LINES_PER_STEP = 2000;
    private static final int SUB_GRAPH_NUM_STEPS = 5;
    private static final int NUM_TRIPLES = 50000;

    private static void compare() throws IOException {
        CompressionStarter hdtStarter = new HDTStarter();
        CompressionStarter grpStarter = new GraphRePairStarter();

        List<File> preparedGraphs = prepareGraphs();
        List<Result> featureResults = analyzeFiles(preparedGraphs);


        List<CompressionResult> resultsHDT = new ArrayList<>();
        List<CompressionResult> resultsGRP = new ArrayList<>();

        List<Double> lstStarSims = new ArrayList<>();
        for (File file : preparedGraphs) {
            CompressionResult resultHDT, resultGRP;
            try {
                final boolean addDictSize = false;
                resultHDT = hdtStarter.compress(file.getAbsolutePath(), "current.hdt", addDictSize);
                resultGRP = grpStarter.compress(file.getAbsolutePath(), null, addDictSize);
            } catch (Exception e) {
                throw new RuntimeException("Compression exception for file " + file.getName());
            }
            resultsHDT.add(resultHDT);
            resultsGRP.add(resultGRP);
            lstStarSims.add(StarPatternAnalyzer.analyzeStarSimilarity(file.getAbsolutePath()));
        }

        System.out.println("\n\n-------\n\nHDT compr ratios");
        for (int i = 0; i < resultsHDT.size(); i++) {
            System.out.print(resultsHDT.get(i).getCompressionRatio() + ",");
        }
        System.out.println("\nGRP compr ratios");
        for (int i = 0; i < resultsGRP.size(); i++) {
            System.out.print(resultsGRP.get(i).getCompressionRatio() + ",");
        }
        System.out.println("\nedge label ratios");
        for (int i = 0; i < resultsHDT.size(); i++) {
            System.out.print(featureResults.get(i).predicateRatio + ",");
        }

        System.out.println("\nstar similarities");
        for (int i = 0; i < lstStarSims.size(); i++) {
            System.out.print(lstStarSims.get(i) + ",");
        }
        System.out.println();


        for (File file : filesToRemove) {
            file.delete();
        }

        System.out.println("\nFiles:");
        for(File graph : preparedGraphs){
            System.out.print(graph.getName()+",");
        }


        writeResultsToFiles(resultsHDT,resultsGRP,featureResults,lstStarSims);

    }

    private static void writeResultsToFiles(List<CompressionResult> resultsHDT, List<CompressionResult> resultsGRP,
                                            List<Result> featureResults, List<Double> lstStarSims) throws IOException {
        File file1 = new File("HDTAndGRPCompareResults/hdtRatios");
        File file2 = new File("HDTAndGRPCompareResults/grpRatios");
        File file3 = new File("HDTAndGRPCompareResults/edgeLabelRatios");
        File file4 = new File("HDTAndGRPCompareResults/starSimis");

        file1.delete();
        file2.delete();
        file3.delete();
        file4.delete();


        StringBuilder sb = new StringBuilder();
        resultsHDT.forEach(r -> sb.append(r.getCompressionRatio()+","));
        Files.write(Paths.get(file1.getAbsolutePath()), sb.toString().getBytes(),StandardOpenOption.CREATE);

        sb.delete(0,sb.length());
        resultsGRP.forEach(r -> sb.append(r.getCompressionRatio()+","));
        Files.write(Paths.get(file2.getAbsolutePath()), sb.toString().getBytes(),StandardOpenOption.CREATE);

        sb.delete(0,sb.length());
        featureResults.forEach(r -> sb.append(r.predicateRatio+","));
        Files.write(Paths.get(file3.getAbsolutePath()), sb.toString().getBytes(),StandardOpenOption.CREATE);

        sb.delete(0,sb.length());
        lstStarSims.forEach(r -> sb.append(r+","));
        Files.write(Paths.get(file4.getAbsolutePath()), sb.toString().getBytes(),StandardOpenOption.CREATE);

    }

    private static List<File> prepareGraphs() throws IOException {

        final String ending = ".sub.ttl";
        for (File file : new File(DIRECTORY).listFiles()) {
            if (file.getName().endsWith(ending)) {
                file.delete();
            }
        }

        List<File> originals = Util.listFilesSorted(DIRECTORY);
        List<File> editedGraphs = new ArrayList<>();

        for (File file : originals) {

            String[] allowedSuffixes = {".nt", ".ttl", ".rdf", ".owl"};
            if (!Util.isSuffixAllowed(file.getAbsolutePath(), allowedSuffixes)) {
                System.out.println("Skipping file " + file.getName());
                continue;
            }

            if (!Util.isFileInNTriplesFormat(file.getAbsolutePath())) {
                System.out.println("Converting file " + file.getName());
                File converted = RDFTurtleConverter.convertAndStoreAsNTriples(file.getAbsolutePath());
                filesToRemove.add(converted);
                file = converted;
            }
            if (file.length() > BOUND_SUBGRAPH) {
                System.out.println("Building sub graph for file " + file.getName());
                String outPath = file.getAbsolutePath() + ending;

                Model model = Util.streamModelFromFile(file.getAbsolutePath(), NUM_TRIPLES);
                Util.writeModelToFile(new File(outPath), model);
                editedGraphs.add(new File(outPath));
            } else {
                editedGraphs.add(file);
            }

        }
        return editedGraphs;
    }

    private static List<Result> analyzeFiles(List<File> files) {
        List<Result> results = new ArrayList<>();
        for (File file : files) {
            Model model = Util.getModelFromFile(file.getAbsolutePath());

            Set<String> foundEdgeUris = new HashSet<>();
            Set<String> foundNodeUris = new HashSet<>();
            ExtendedIterator<Triple> iterator = model.getGraph().find();
            while (iterator.hasNext()) {
                Triple triple = iterator.next();
                String uri = triple.getPredicate().getURI();
                foundEdgeUris.add(uri);
                if (triple.getSubject().isURI()) {
                    foundNodeUris.add(triple.getSubject().getURI());
                }
                if (triple.getObject().isURI()) {
                    foundNodeUris.add(triple.getObject().getURI());
                }
            }
            int size = model.getGraph().size();
            results.add(new Result(1.0 * foundEdgeUris.size() / size, 1.0 * foundNodeUris.size(), size));
        }
        return results;
    }

    public static void main(String[] args) throws IOException {
        compare();
    }

    private static class Result {

        double predicateRatio;
        double nodeLabelRatio;
        int numTriples;

        public Result(double predicateRatio, double nodeLabelRatio, int numTriples) {
            this.predicateRatio = predicateRatio;
            this.nodeLabelRatio = nodeLabelRatio;
            this.numTriples = numTriples;
        }
    }
}
