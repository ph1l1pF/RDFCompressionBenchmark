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
            compressionResultsHDT.add(hdtStarter.compress(filePath));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGRP.add(graphRePairStarter.compress(filePath));
        }

        System.out.println("\n\n\n\n-----------------------");

        for (List<Triple> graph : graphs) {
            distributePredicates(graph);
            System.out.print(graph.size() + ", ");
        }
        System.out.println();

        System.out.println("HDT compression ratios:");
        for (CompressionResult compressionResult : compressionResultsHDT) {
            double compressionRatio = compressionResult.getCompressionRatio();
            compressionRatio = Math.floor(compressionRatio * 100000) / 100000;
            System.out.print( compressionRatio+", ");
        }

        System.out.println("\n\n GRP compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGRP) {
            double compressionRatio = compressionResult.getCompressionRatio();
            compressionRatio = Math.floor(compressionRatio * 10000) / 10000;
            System.out.print(compressionRatio + ", ");
        }
//        compressionResultsGPR.stream().mapToDouble(c -> c.getCompressionRatio()).summaryStatistics().

        // immer gleiche anzahl an knoten, anzahl kanten ergibt sich dann
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
        for (int i = 0; i < 1; i++) {
            predicates.add(String.valueOf(i));
        }
        predicateDistributor.distributePredicates(graph, predicates);
    }

    public static void main(String[] args) {
        evaluateStarGraphs();
//        testRandomStars();
    }
}
