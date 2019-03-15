package compressionHandling;

public class CompressionResult implements Comparable {

    private long originalSize, compressedSize,compressionTime, decompressionTime;

    private String fileName;

    public CompressionResult(long originalSize, long compressedSize, long compressionTime, long decompressionTime, String fileName) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionTime = compressionTime;
        this.decompressionTime = decompressionTime;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "original size: " + originalSize + "\n"+
                "compressed size: " + compressedSize + "\n"+
                "compression ratio: " + getCompressionRatio()+"\n"+
                "compression time: "+compressionTime+"\n"+
                "decompressionTime: " + decompressionTime;
    }

    public double getCompressionRatio(){
        return 1.0*compressedSize/originalSize;
    }

    public long getOriginalSize() {
        return originalSize;
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
