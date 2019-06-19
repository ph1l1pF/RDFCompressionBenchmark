package evaluation;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.HDTStarter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class LiteralAndBlankNodeAnalyzer {
    private static String dir = "finalGraph";

    private static final boolean HUFFMAN = false;
    private static final boolean BLANK_SHORT_IDS = true;
    private static final boolean BLANK_NO_IDS = true;


    public static void main(String[] args) throws IOException {
//        getRatios();
        getFeatures();
    }

    private static void getRatios() {
        HDTStarter hdtStarter = new HDTStarter();
        List<File> files = Util.listFilesSorted(dir);
        List<Double> ratiosNormal = new ArrayList<>();
        List<Double> ratiosHuff = new ArrayList<>();

        List<Long> runTimesNormal = new ArrayList<>();
        List<Long> runTimesHuff = new ArrayList<>();


        System.out.println("\nFiles");
        files.forEach(f->System.out.print(f.getName()+","));

        for (File file : files) {
            if(file.isFile()) {
                CompressionResult result = hdtStarter.compress(file.getAbsolutePath(), "bla.hdt", true);
                ratiosNormal.add(1.0 * result.getCompressionDictionarySize() / result.getOriginalSize());
                runTimesNormal.add(result.getCompressionTime());
            }
        }


        hdtStarter.setHuffmanActive(HUFFMAN);
        hdtStarter.setOmitBlankNodeIds(BLANK_NO_IDS);
        hdtStarter.setShortBlankNodeIds(BLANK_SHORT_IDS);


        for (File file : files) {
            if(file.isFile()) {
                CompressionResult result = hdtStarter.compress(file.getAbsolutePath(), "bla.hdt", true);
                ratiosHuff.add(1.0 * result.getCompressionDictionarySize() / result.getOriginalSize());
                runTimesHuff.add(result.getCompressionTime());
            }
        }

        System.out.println("\n\nRatios");
        System.out.println("Normal");
        ratiosNormal.forEach(x -> System.out.print(x+","));
        System.out.println("\nImproved");
        ratiosHuff.forEach(x -> System.out.print(x+","));

        System.out.println("\n\nRun Times");
        System.out.println("Normal");
        runTimesNormal.forEach(x -> System.out.print(x+","));
        System.out.println("\nImproved");
        runTimesHuff.forEach(x -> System.out.print(x+","));

    }

    private static void getFeatures() throws IOException {
        List<File> files = Util.listFilesSorted(dir);

        System.out.println("\nFiles");
        files.forEach(f->System.out.print(f.getName()+","));

        List<Integer> numTriples = new ArrayList<>();
        List<List<Integer>> lengths = new ArrayList<>();
        List<Double> averageLength = new ArrayList<>();
        List<Double> relativePortions = new ArrayList<>();
        List<Integer> triplesWithBlanks = new ArrayList<>();

        for (File file : files) {
            if(!file.isFile()){
                continue;
            }
            List<Integer> currLengths = new ArrayList<>();
            Model model = Util.getModelFromFile(file.getAbsolutePath());
            ExtendedIterator<Triple> iterator = model.getGraph().find();

            int countLiterals = 0;
            int triplesWithBlankNodes =0;
            while (iterator.hasNext()) {
                Triple triple = iterator.next();
                Node object = triple.getObject();
                if (object.isLiteral()) {
                    currLengths.add(object.getLiteral().toString().length());
                    countLiterals++;
                }

                if(triple.getSubject().isBlank() || object.isBlank()){
                    triplesWithBlankNodes++;
                }
            }

            lengths.add(currLengths);
            triplesWithBlanks.add(triplesWithBlankNodes);
            numTriples.add(model.getGraph().size());

            int sum = 0;
            for(Integer length : currLengths){
                sum+=length;
            }

            averageLength.add(1.0*sum/currLengths.size());
            relativePortions.add(1.0 * countLiterals / model.getGraph().size());
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (List<Integer> currLengths : lengths) {
            for (Integer length : currLengths) {
                stringBuilder.append(length + ",");
            }
            stringBuilder.append("\n");
        }
        File fileOut = new File("literalAnalysis/allLength");
        fileOut.delete();
        Files.write(Paths.get(fileOut.getAbsolutePath()), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE);

        // print results
        System.out.println("\n\nAverage Lengths");
        averageLength.forEach(x -> System.out.print(x+","));
        System.out.println("\nRelative Literal Amounts");
        relativePortions.forEach(x -> System.out.print(x+","));
        System.out.println("\nTriples ");
        numTriples.forEach(x -> System.out.print(x+","));
        System.out.println("\nTriples With blank nodes");
        triplesWithBlanks.forEach(x -> System.out.print(x+","));
    }
}
