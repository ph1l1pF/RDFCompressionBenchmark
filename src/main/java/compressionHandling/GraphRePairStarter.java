package compressionHandling;

import GraphRePair.GraphStatistics;
import GraphRePair.Start;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphRePairStarter implements CompressionStarter {

    private int maxRank;

    public GraphRePairStarter(){
        // standard value
        maxRank = 4;
    }

    public GraphRePairStarter(int maxRank) {
        this.maxRank = maxRank;
    }

    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        // remove old compression output
        File directoryOutput = new File("output_" + filePath);

        try {
            FileUtils.deleteDirectory(directoryOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // compute the HDT dictionary size and add it the compression result size
        final long dictionarySize = getHDTDictionarySize(filePath);
        long compressedSize = 0;
        if (addDictionarySizeToCompressedSize) {
            compressedSize += dictionarySize;
        }

        // start new compression
        long compressionTime = System.currentTimeMillis();
        Start.main(new String[]{"compress", filePath, "t=rdf", "maxRank="+maxRank, "order=none"});
        compressionTime = System.currentTimeMillis() - compressionTime;

        File inputFile = new File(filePath);
        long originalSize = inputFile.length();


        for (File file : directoryOutput.listFiles()) {
            if (file.isFile()) {
                compressedSize += file.length();
            }
        }

        try {
            FileUtils.deleteDirectory(directoryOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new CompressionResult(originalSize, compressedSize, dictionarySize, compressionTime, -1, inputFile.getAbsolutePath());
    }

    public CompressionResult decompress(String file) {
        Start.main(new String[]{"decompress output/", file, "out=" + "lo"});

        return null;

    }

    private static long getHDTDictionarySize(String filePath) {
        File outputFile = new File(filePath + ".hdt");
        CompressionResult compressionResult = new HDTStarter().compress(filePath, outputFile.getAbsolutePath(), true);
        outputFile.delete();
        return compressionResult.getCompressionDictionarySize();
    }


}
