package compressionHandling;

import GraphRePair.Start;

import java.io.File;

public class GraphRePairStarter implements CompressionStarter {

    public  CompressionResult compress(String filePath){

        // remove old compression output
        File directoryOutput = new File("output_" + filePath);
        if (directoryOutput.exists()) {
            for (File file : directoryOutput.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        // start new compression
        long compressionTime = System.currentTimeMillis();
        Start.main(new String[]{"compress",filePath, "t=rdf"});
        compressionTime = System.currentTimeMillis()-compressionTime;

        File inputFile = new File(filePath);
        long originalSize = inputFile.length();

        long compressedSize = 0;
        for (File file : directoryOutput.listFiles()) {
            if (file.isFile()) {
                compressedSize += file.length();
            }
        }

        return new CompressionResult(originalSize, compressedSize, compressionTime, -1, inputFile.getAbsolutePath());
    }

    public CompressionResult decompress(String file){
        Start.main(new String[]{"decompress output/",file, "out="+ "lo"});

        return null;

    }
}
