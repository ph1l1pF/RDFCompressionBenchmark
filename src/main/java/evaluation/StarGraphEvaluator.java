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

    private static final String HTTP_PREFIX_SUBJECT = "http://subject/";
    public static final String HTTP_PREFIX_PREDICATE = "http://predicate/";
    private static final String HTTP_PREFIX_OBJECT = "http://object/";

    private static void evaluateStarGraphs() {
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphsWithFixedSize();

        for (List<Triple> graph : graphs) {
            distributePredicates(graph, 1000);
        }
        evaluateCompressors(graphs);
    }


    private static void evaluatePredicateAmount() {
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
        evaluateCompressors(graphs);

    }

    private static void evaluateCompressors(List<List<Triple>> graphs) {
        List<CompressionResult> compressionResultsHDT = new ArrayList<>();
        List<CompressionResult> compressionResultsGRP = new ArrayList<>();

        int count = 0;
        for (List<Triple> graph : graphs) {

            String filePath = "file.ttl";
            Util.Util.writeTriplesToFile(graph, filePath, HTTP_PREFIX_PREDICATE);

            HDTStarter hdtStarter = new HDTStarter();
            compressionResultsHDT.add(hdtStarter.compress(filePath, "fileCompressedWithHDT.hdt", true));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGRP.add(graphRePairStarter.compress(filePath, null, true));

            System.out.println("\n\n\n\n-----------------------");
            System.out.println(100.0 * count / graphs.size() + "% done");

            count++;
        }

        printResults(compressionResultsHDT, compressionResultsGRP);
    }


    private static void printResults(List<CompressionResult> compressionResultsHDT, List<CompressionResult> compressionResultsGRP) {
        System.out.println("\n\n\n\n-----------------------");

        System.out.println("HDT compression ratios:");
        for (CompressionResult compressionResult : compressionResultsHDT) {
            System.out.print(compressionResult.getCompressionRatio() + ", ");
        }

        System.out.println("\n\n GRP compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGRP) {
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

    public static void main(String[] args) {
//        evaluatePredicateAmount();
        evaluateStarGraphs();
    }
}
