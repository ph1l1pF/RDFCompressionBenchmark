package evaluation;

import GraphGeneration.StarPatternGenerator;
import PredicateHandling.PredicateDistributor;
import PredicateHandling.RandomPredicateDistributor;
import Util.Triple;
import compressionHandling.CompressionResult;
import compressionHandling.GraphRePairStarter;
import compressionHandling.GzipStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.ext.com.google.common.io.Files;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarGraphEvaluator {

    private static int numTriples = -1;

    private static List<Double> lstStartPatternSimilarities = new ArrayList<>();

    private static EvalResult evaluateStarGraphs(int numPredicates) {
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphsWithFixedSize();

        numTriples = graphs.get(0).size();
        for (List<Triple> graph : graphs) {
            distributePredicates(graph, numPredicates);
        }
        return evaluateCompressors(graphs);
    }


    private static EvalResult evaluatePredicateAmount() {
        List<Triple> initGraph = StarPatternGenerator.generateMultipleStarPatternGraphsWithFixedSize().get(0);

        List<List<Triple>> graphs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<Triple> newGraph = new ArrayList<>();
            for (Triple triple : initGraph) {
                newGraph.add(triple.deepCopy());
            }
            graphs.add(newGraph);
            distributePredicates(newGraph, i + 1);
        }
        return evaluateCompressors(graphs);

    }

    private static void evalDecompression() {
        String dir = "stargraphs";
        GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
        List<Long> decompressionTimes = new ArrayList<>();
        for (int i = 5; i < new File(dir).listFiles().length; i++) {
            String currFile = dir + "/" + i + "/file.ttl.gr.gr";
            long time = graphRePairStarter.decompress(currFile);
            decompressionTimes.add(time);
        }
    }

    private static EvalResult evaluateCompressors(List<List<Triple>> graphs) {
        List<CompressionResult> compressionResultsHDT = new ArrayList<>();
        List<CompressionResult> compressionResultsGRP = new ArrayList<>();
        List<CompressionResult> compressionResultsGzip = new ArrayList<>();

        int count = 0;
        for (List<Triple> graph : graphs) {

            String dir = "stargraphs/" + count;

            if (!new File(dir).exists()) {
                new File(dir).mkdir();

            }
            String filePath = dir + "/file.ttl";
            Util.Util.writeTriplesToFile(graph, filePath);

            lstStartPatternSimilarities.add(StarPatternAnalyzer.analyzeStarSimilarity(filePath));

            final boolean addDictSize = false;

            HDTStarter hdtStarter = new HDTStarter();
            compressionResultsHDT.add(hdtStarter.compress(filePath, "fileCompressedWithHDT.hdt", addDictSize));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGRP.add(graphRePairStarter.compress(filePath, null, addDictSize));

            GzipStarter gzipStarter = new GzipStarter();
            compressionResultsGzip.add(gzipStarter.compress(filePath, "fileCompressedWithGzip.gzip", addDictSize));

            System.out.println("\n\n\n\n-----------------------");
            System.out.println(100.0 * count / graphs.size() + "% done");

            count++;
        }

//        printResults(compressionResultsHDT, compressionResultsGRP, compressionResultsGzip);

        for (double starPa : lstStartPatternSimilarities) {
            System.out.print(starPa + ",");
        }

        return new EvalResult(compressionResultsHDT, compressionResultsGRP, compressionResultsGzip);
    }


    private static void printResults(List<CompressionResult> compressionResultsHDT, List<CompressionResult> compressionResultsGRP, List<CompressionResult> compressionResultsGzip) {
        System.out.println("\n\n\n\n-----------------------");

        System.out.println("HDT compression ratios:");
        for (CompressionResult compressionResult : compressionResultsHDT) {
            System.out.print(compressionResult.getCompressionRatio() + ", ");
        }

        System.out.println("\n\n GRP compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGRP) {
            System.out.print(compressionResult.getCompressionRatio() + ", ");
        }

        System.out.println("\n\n Gzip compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGzip) {
            System.out.print(compressionResult.getCompressionRatio() + ", ");
        }

    }


    private static void distributePredicates(List<Triple> graph, int numPredicates) {
        PredicateDistributor predicateDistributor = new RandomPredicateDistributor();
        List<String> predicates = new ArrayList<>();
        for (int i = 0; i < numPredicates; i++) {
            predicates.add(String.valueOf(i));
        }
        predicateDistributor.distributePredicates(graph, predicates);
    }

    private static class EvalResult {
        List<CompressionResult> compressionResultsHDT, compressionResultsGRP, compressionResultsGzip;

        public EvalResult(List<CompressionResult> compressionResultsHDT, List<CompressionResult> compressionResultsGRP, List<CompressionResult> compressionResultsGzip) {
            this.compressionResultsHDT = compressionResultsHDT;
            this.compressionResultsGRP = compressionResultsGRP;
            this.compressionResultsGzip = compressionResultsGzip;
        }
    }

    private static void evaluateRunTimes(List<EvalResult> evalResults) {
        // compute average run time
        long[] sumsHDT = new long[evalResults.get(0).compressionResultsHDT.size()];
        long[] sumsGRP = new long[evalResults.get(0).compressionResultsGRP.size()];
        for (EvalResult evalResult : evalResults) {
            for (int i = 0; i < evalResult.compressionResultsHDT.size(); i++) {
                sumsHDT[i] += evalResult.compressionResultsHDT.get(i).getCompressionTime();
                sumsGRP[i] += evalResult.compressionResultsGRP.get(i).getCompressionTime();
            }
        }

        double[] avgsHDT = new double[sumsHDT.length];
        double[] avgsGRP = new double[sumsGRP.length];

        for (int i = 0; i < avgsHDT.length; i++) {
            avgsHDT[i] = 1.0 * sumsHDT[i] / sumsHDT.length;
            avgsGRP[i] = 1.0 * sumsGRP[i] / sumsGRP.length;
        }

        System.out.println("--------------\n\nAverage run times:\n\n");

        for (int i = 0; i < avgsHDT.length; i++) {
            System.out.print(avgsHDT[i] + ",");
        }

        System.out.println();
        for (int i = 0; i < avgsGRP.length; i++) {
            System.out.print(avgsGRP[i] + ",");
        }
    }

    private static void evalCompression() throws IOException {
        //        evaluatePredicateAmount();

        // multiple runnings for runtime measurement

        List<EvalResult> evalResults = new ArrayList<>();
        int maxPredicateValue = -1;
        int i = 0;
        int indexHDTGetsBetter = -1;
        for (int predicates = 1; predicates <= 1; predicates += 15) {

            EvalResult result = evaluateStarGraphs(predicates);
            evalResults.add(result);

            for (int j = 0; j < result.compressionResultsHDT.size(); j++) {
                if (result.compressionResultsHDT.get(j).getCompressionRatio() < result.compressionResultsGRP.get(j).getCompressionRatio()) {
                    if (maxPredicateValue == -1) {
                        maxPredicateValue = predicates;
                    }
                    if (indexHDTGetsBetter == -1) {
                        indexHDTGetsBetter = i;
                    }
                }
            }
            i++;
        }

        System.out.println("\nat this ELR HDT gets better: " + 1.0 * maxPredicateValue / numTriples);
        System.out.println("at this index: " + indexHDTGetsBetter);

        File fileStarPatternResultsHDT = new File("starPatternResultsHDT.txt");
        fileStarPatternResultsHDT.delete();
        File fileStarPatternResultsGRP = new File("starPatternResultsGRP.txt");
        fileStarPatternResultsGRP.delete();

        StringBuilder sb = new StringBuilder();
        for (EvalResult evalResult : evalResults) {

            for (CompressionResult resultHDT : evalResult.compressionResultsHDT) {
                sb.append(resultHDT.getCompressionRatio() + ",");
            }
            sb.append("\n");
        }
        Files.write(sb.toString().getBytes(), fileStarPatternResultsHDT);

        sb = new StringBuilder();


        for (EvalResult evalResult : evalResults) {

            for (CompressionResult resultGRP : evalResult.compressionResultsGRP) {
                sb.append(resultGRP.getCompressionRatio() + ",");
            }
            sb.append("\n");
        }
        Files.write(sb.toString().getBytes(), fileStarPatternResultsGRP);
    }

    public static void main(String[] args) throws IOException {

        evalDecompression();
    }
}
