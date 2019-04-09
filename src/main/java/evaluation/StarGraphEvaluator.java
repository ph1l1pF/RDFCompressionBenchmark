package evaluation;

import GraphGeneration.StarPatternGenerator;
import PredicateHandling.PredicateDistributor;
import PredicateHandling.RandomPredicateDistributor;
import Util.Triple;
import compressionHandling.CompressionResult;
import compressionHandling.GraphRePairStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.ext.com.google.common.io.Files;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarGraphEvaluator {

    private static EvalResult evaluateStarGraphs() {
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphsWithFixedSize();

        for (List<Triple> graph : graphs) {
            distributePredicates(graph, 1);
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

    private static EvalResult evaluateCompressors(List<List<Triple>> graphs) {
        List<CompressionResult> compressionResultsHDT = new ArrayList<>();
        List<CompressionResult> compressionResultsGRP = new ArrayList<>();

        int count = 0;
        for (List<Triple> graph : graphs) {

            String filePath = "file.ttl";
            Util.Util.writeTriplesToFile(graph, filePath);

            HDTStarter hdtStarter = new HDTStarter();
            compressionResultsHDT.add(hdtStarter.compress(filePath, "fileCompressedWithHDT.hdt", true));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGRP.add(graphRePairStarter.compress(filePath, null, true));

            System.out.println("\n\n\n\n-----------------------");
            System.out.println(100.0 * count / graphs.size() + "% done");

            count++;
        }

        printResults(compressionResultsHDT, compressionResultsGRP);

        return new EvalResult(compressionResultsHDT, compressionResultsGRP);
    }


    private static void printResults(List<CompressionResult> compressionResultsHDT, List<CompressionResult> compressionResultsGRP) {
        System.out.println("\n\n\n\n-----------------------");

        System.out.println("HDT compression ratios:");
        for (CompressionResult compressionResult : compressionResultsHDT) {
            System.out.print(compressionResult.getCompressionTime() + ", ");
        }

        System.out.println("\n\n GRP compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGRP) {
            System.out.print(compressionResult.getCompressionTime() + ", ");
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
        List<CompressionResult> compressionResultsHDT, compressionResultsGRP;

        public EvalResult(List<CompressionResult> compressionResultsHDT, List<CompressionResult> compressionResultsGRP) {
            this.compressionResultsHDT = compressionResultsHDT;
            this.compressionResultsGRP = compressionResultsGRP;
        }
    }

    public static void main(String[] args) {
//        evaluatePredicateAmount();

        // multiple runnings for runtime measurement

        List<EvalResult> evalResults = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            evalResults.add(evaluateStarGraphs());
        }

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
}
