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
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphs();

        for (List<Triple> graph : graphs) {
            distributePredicates(graph);
        }
        List<CompressionResult> compressionResultsHDT = new ArrayList<>();
        List<CompressionResult> compressionResultsGPR = new ArrayList<>();

        for (List<Triple> graph : graphs) {

            String filePath = "file.ttl";
            writeTriplesToFile(graph, filePath);

            HDTStarter hdtStarter = new HDTStarter();
            compressionResultsHDT.add(hdtStarter.compress(filePath));

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
            compressionResultsGPR.add(graphRePairStarter.compress(filePath));
        }

        System.out.println("\n\n\n\n-----------------------");
        System.out.println("HDT compression ratios:");
        for (CompressionResult compressionResult : compressionResultsHDT) {
            double compressionRatio = compressionResult.getCompressionRatio();
            compressionRatio = Math.floor(compressionRatio * 100000) / 100000;
            System.out.print( compressionRatio+", ");
        }

        System.out.println("\n\n GPR compression ratios:");
        for (CompressionResult compressionResult : compressionResultsGPR) {
            double compressionRatio = compressionResult.getCompressionRatio() * 10000;
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

    private static void testRandomStars() {
        int numTriples = 1000;
        int cores = 400;

        List<Triple> triples = new ArrayList<>();
        for (int i = 0; i < numTriples; i++) {
            int core = new Random().nextInt(cores);
            int leaf = new Random().nextInt(10000);

            Triple triple;
            if (true) {
                triple = new Triple(String.valueOf(core), "-", String.valueOf(leaf));
            } else {
                triple = new Triple(String.valueOf(leaf), "-", String.valueOf(core));
            }
            triples.add(triple);
        }
        String filePath = "random.ttl";
        writeTriplesToFile(triples, filePath);

        HDTStarter hdtStarter = new HDTStarter();
        CompressionResult resultHDT = hdtStarter.compress(filePath);

        GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
        CompressionResult resultGPR = graphRePairStarter.compress(filePath);

        System.out.println("HDT: " + resultHDT.getCompressionRatio());
        System.out.println("GPR: " + resultGPR.getCompressionRatio());


    }

    private static void distributePredicates(List<Triple> graph) {
        PredicateDistributor predicateDistributor = new RandomPredicateDistributor();
        List<String> predicates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            predicates.add(String.valueOf(i));
        }
        predicateDistributor.distributePredicates(graph, predicates);
    }

    public static void main(String[] args) {
        evaluateStarGraphs();
//        testRandomStars();
    }
}
