package compressionHandling;

import compressionHandling.CompressionResult;

public interface CompressionStarter {

    CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize);

    CompressionResult decompress(String filePath);
}
