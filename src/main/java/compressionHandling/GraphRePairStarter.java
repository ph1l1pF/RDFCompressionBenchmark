package compressionHandling;

import GraphRePair.Start;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraphRePairStarter implements CompressionStarter {

    private int maxRank;

    private  boolean omitBlankNodeIds=false;
    private boolean huffmanActive=false;


    public GraphRePairStarter() {
        // standard value
        maxRank = 4;

    }


    public GraphRePairStarter(boolean omitBlankNodeIds, boolean huffmanActive) {
        this.omitBlankNodeIds = omitBlankNodeIds;
        this.huffmanActive = huffmanActive;
    }




    public GraphRePairStarter(int maxRank) {
        this.maxRank = maxRank;
    }

    private List<File> getOutputFiles(String fileName) {
        List<File> outputFiles = new ArrayList<>();
        outputFiles.add(new File(fileName + ".gr.gr.multi"));
        outputFiles.add(new File(fileName + ".gr.gr.P"));
        outputFiles.add(new File(fileName + ".gr.gr.perms"));
        outputFiles.add(new File(fileName + ".gr.gr.S"));
        outputFiles.add(new File(fileName + ".gr.gr.ids"));
        return outputFiles;
    }

    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        // remove old compression output
        List<File> outputFiles = getOutputFiles(filePath);

        for (File file : outputFiles) {
            file.delete();
        }

        // compute the HDT dictionary size and add it the compression result size
        final long dictionarySize = getHDTDictionarySize(filePath);
        long compressedSize = 0;
        if (addDictionarySizeToCompressedSize) {
            compressedSize += dictionarySize;
        }

        // start new compression
        long compressionTime = System.currentTimeMillis();
        Start.main(new String[]{"compress", filePath, "t=rdf", "maxRank=" + maxRank, "order=none"/*"minSaving=8"*/});
        compressionTime = System.currentTimeMillis() - compressionTime;

        File inputFile = new File(filePath);
        long originalSize = inputFile.length();


        for (File file : outputFiles) {
            compressedSize += file.length();
        }

//        for (File file : outputFiles) {
//            file.delete();
//        }


        return new CompressionResult(originalSize, compressedSize, dictionarySize, compressionTime, -1, inputFile.getAbsolutePath());
    }

    public long decompress(String grammarBaseName) {
        long time = System.currentTimeMillis();
        Start.main(new String[]{"decompress", grammarBaseName, "out=" + "lo"});
        return System.currentTimeMillis() - time;
    }

    private long getHDTDictionarySize(String filePath) {
        File outputFile = new File(filePath + ".hdt");
        CompressionResult compressionResult = new HDTStarter(omitBlankNodeIds, huffmanActive).compress(filePath, outputFile.getAbsolutePath(), true);
        outputFile.delete();
        return compressionResult.getCompressionDictionarySize();
    }


    public int getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(int maxRank) {
        this.maxRank = maxRank;
    }

    public boolean isOmitBlankNodeIds() {
        return omitBlankNodeIds;
    }

    public void setOmitBlankNodeIds(boolean omitBlankNodeIds) {
        this.omitBlankNodeIds = omitBlankNodeIds;
    }

    public boolean isHuffmanActive() {
        return huffmanActive;
    }

    public void setHuffmanActive(boolean huffmanActive) {
        this.huffmanActive = huffmanActive;
    }
}
