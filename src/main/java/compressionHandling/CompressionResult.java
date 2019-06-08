package compressionHandling;

public class CompressionResult implements Comparable {

    private long originalSize, compressedSize, compressionDictionarySize, compressionTime, decompressionTime;

    private String fileName;

    public CompressionResult(long originalSize, long compressedSize, long compressionDictionarySize, long compressionTime, long decompressionTime, String fileName) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionDictionarySize = compressionDictionarySize;
        this.compressionTime = compressionTime;
        this.decompressionTime = decompressionTime;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return  "Compression Result: {\n"+
                "file name: "+ fileName + "\n"+
                "original size: " + originalSize + "\n"+
                "compressed size: " + compressedSize + "\n"+
                "compressed dict size: " + compressionDictionarySize + "\n"+
                "compression ratio: " + getCompressionRatio()+"\n"+
                "compression time: "+compressionTime+"\n"+
                "decompressionTime: " + decompressionTime+"\n"
                +"}\n";
    }

    public double getCompressionRatio(){
        return 1.0*compressedSize/originalSize;
    }

    public double getCompressionRatioRalatedToDict(){
        return 1.0*compressionDictionarySize/originalSize;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCompressionDictionarySize() {
        return compressionDictionarySize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getCompressionTime() {
        return compressionTime;
    }

    public long getDecompressionTime() {
        return decompressionTime;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int compareTo(Object o) {
        CompressionResult result = (CompressionResult) o;
        if (getCompressionRatio() == result.getCompressionRatio()) {
            return 0;
        }
        if (getCompressionRatio() < result.getCompressionRatio()) {
            return -1;
        } else {
            return 1;
        }
    }
}
