package compressionHandling;

public class CompressionResult {

    private long originalSize, compressedSize,compressionTime, decompressionTime;

    public CompressionResult(long originalSize, long compressedSize, long compressionTime, long decompressionTime) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionTime = compressionTime;
        this.decompressionTime = decompressionTime;
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
}
