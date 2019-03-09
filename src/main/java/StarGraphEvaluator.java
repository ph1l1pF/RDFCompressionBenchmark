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

    private static void evaluateStarGraphs() {
        List<List<Triple>> graphs = StarPatternGenerator.generateMultipleStarPatternGraphs();
        List<CompressionResult> compressionResultsHDT = new ArrayList<CompressionResult>();
        List<CompressionResult> compressionResultsGPR = new ArrayList<CompressionResult>();

        int minSize = Integer.MAX_VALUE;
        for(List<Triple> graph:graphs){
            if(graph.size()<minSize){
                minSize=graph.size();
            }
        }
        for(List<Triple> graph:graphs){
            while(graph.size()>minSize){
                graph.remove(new Random().nextInt(minSize));
            }
        }

        for (List<Triple> graph : graphs) {
            StringBuilder sb = new StringBuilder();
            for (Triple triple : graph) {
                sb.append("<" + triple.getSubject() + "> <" + triple.getPredicate() + "> <" + triple.getObject() + "> .\n");
            }

            String filePath = "file.ttl";
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
            try {
                Files.write(sb.toString().getBytes(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }

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


//        for (CompressionResult compressionResult : compressionResults) {
//            System.out.print(compressionResult.getOriginalSize() + ", ");
//        }
    }

    public static void main(String[] args) {
        evaluateStarGraphs();
    }
}
