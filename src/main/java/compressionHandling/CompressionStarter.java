package compressionHandling;

import compressionHandling.CompressionResult;

public interface CompressionStarter {

    public CompressionResult compress(String filePath);

    public CompressionResult decompress(String filePath);
}
