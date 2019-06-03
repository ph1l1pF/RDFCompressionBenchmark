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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HDTAndGRPComparer {

    private static final String DIRECTORY = "/Users/philipfrerk/Documents/RDF_data/MIXED";

    private static List<File> filesToRemove = new ArrayList<>();

    private static final long BOUND_SUBGRAPH = 20000000;

    private static void compare() throws IOException {
        CompressionStarter hdtStarter = new HDTStarter();
        CompressionStarter grpStarter = new GraphRePairStarter();

        List<File> subgraphs = buildEntityBasedSubGraphs();
        List<Result> featureResults = analyzeFiles(subgraphs);

        List<CompressionResult> resultsHDT = new ArrayList<>();
        List<CompressionResult> resultsGRP = new ArrayList<>();

        for (File file : subgraphs) {
            try {
                resultsGRP.add(grpStarter.compress(file.getAbsolutePath(), null, false));
                resultsHDT.add(hdtStarter.compress(file.getAbsolutePath(), "current.hdt", false));
            } catch (Exception e) {
                continue;
            }
        }

        System.out.println("\n\n-------\n\nHDT compr ratios");
        for (int i = 0; i < resultsHDT.size(); i++) {
            System.out.print(resultsHDT.get(i).getCompressionRatio()+",");
        }
        System.out.println("\nGRP compr ratios");
        for (int i = 0; i < resultsHDT.size(); i++) {
            System.out.print(resultsGRP.get(i).getCompressionRatio()+",");
        }
        System.out.println("\nedge label ratios");
        for (int i = 0; i < resultsHDT.size(); i++) {
            System.out.print(featureResults.get(i).predicateRatio+",");
        }


        for(File file: filesToRemove){
            file.delete();
        }

    }

    private static List<File> buildEntityBasedSubGraphs() throws IOException {

        final String ending = ".sub.ttl";
        for (File file : new File(DIRECTORY).listFiles()) {
            if (file.getName().endsWith(ending)) {
                file.delete();
            }
        }

        List<File> originals = Util.listFilesSorted(DIRECTORY);
        List<File> subgraphs = new ArrayList<>();

        for (File original : originals) {

            if (original.getAbsolutePath().endsWith(".rdf")||original.getAbsolutePath().endsWith(".owl")) {
                original = RDFTurtleConverter.convertAndStoreAsNTriples(original.getAbsolutePath());
                filesToRemove.add(original);
            }
            if (original.getAbsolutePath().endsWith(".ttl")||original.getAbsolutePath().endsWith(".nt")) {
                if (original.length()>BOUND_SUBGRAPH) {
                    String entity = Util.getEntityFromGraph(original.getAbsolutePath());
                    String outPath = original.getAbsolutePath() + ending;
                    Util.createEntityBasedSubGraph(entity, original.getAbsolutePath(), outPath, 5, 2000);
                    subgraphs.add(new File(outPath));
                } else {
                    subgraphs.add(original);
                }
            } else {
                System.out.println("Skipping file " + original.getName());
            }
        }
        return subgraphs;
    }

    private static List<Result> analyzeFiles(List<File> files) {
        List<Result> results = new ArrayList<>();
        for (File file : files) {
            Model model = Util.getModelFromFile(file.getAbsolutePath());

            Set<String> foundEdgeUris = new HashSet<>();
            Set<String> foundNodeUris = new HashSet<>();
            ExtendedIterator<Triple> iterator = model.getGraph().find();
            while(iterator.hasNext()){
                Triple triple = iterator.next();
                String uri = triple.getPredicate().getURI();
                foundEdgeUris.add(uri);
                if(triple.getSubject().isURI()){
                    foundNodeUris.add(triple.getSubject().getURI());
                }
                if(triple.getObject().isURI()){
                    foundNodeUris.add(triple.getObject().getURI());
                }
            }
            int size = model.getGraph().size();
            results.add(new Result(1.0*foundEdgeUris.size()/size, 1.0*foundNodeUris.size(),size));
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
            this.nodeLabelRatio= nodeLabelRatio;
            this.numTriples=numTriples;
        }
    }
}
