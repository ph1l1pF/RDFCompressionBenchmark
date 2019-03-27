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

    private static final String HTTP_PREFIX = "http://myurl/";

    private static void evaluateStarGraphs() {
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphsWithFixedSize();

        for (List<Triple> graph : graphs) {
            distributePredicates(graph);
        }

        List<CompressionResult> compressionResultsHDT = new ArrayList<>();
        List<CompressionResult> compressionResultsGRP = new ArrayList<>();

        for (List<Triple> graph : graphs) {

            String filePath = "file.ttl";
            writeTriplesToFile(graph, filePath);

            HDTStarter hdtStarter = new HDTStarter();
            compressionResultsHDT.add(hdtStarter.compress(filePath, "fileCompressedWithHDT.hdt", true));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGRP.add(graphRePairStarter.compress(filePath, null, true));
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


    private static void writeTriplesToFile(List<Triple> triples, String filePath) {
        StringBuilder sb = new StringBuilder();
        for (Triple triple : triples) {
            sb.append("<" + HTTP_PREFIX + triple.getSubject() + "> <" + HTTP_PREFIX + triple.getPredicate() + "> <"
                    + HTTP_PREFIX + triple.getObject() + "> .\n");
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            Files.write(sb.toString().getBytes(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void distributePredicates(List<Triple> graph) {
        PredicateDistributor predicateDistributor = new RandomPredicateDistributor();
        List<String> predicates = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            predicates.add(String.valueOf(i));
        }
        predicateDistributor.distributePredicates(graph, predicates);
    }

    public static void main(String[] args) {
        evaluateStarGraphs();
    }
}
