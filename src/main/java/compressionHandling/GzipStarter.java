package compressionHandling;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class GzipStarter implements CompressionStarter {


    @Override
    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        long compressionTime = System.currentTimeMillis();
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outputName))) {
            try (FileInputStream in = new FileInputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            compressionTime = System.currentTimeMillis() - compressionTime;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File inputFile = new File(filePath);
        File outputFile = new File(outputName);

        return new CompressionResult(inputFile.length(), outputFile.length(), -1, compressionTime, -1, inputFile.getAbsolutePath());


    }

    @Override
    public long decompress(String filePath) {
        return 0;
    }


    public static void main(String[] a) {
        GzipStarter gzipStarter = new GzipStarter();
        gzipStarter.compress("/Users/philipfrerk/github/MastersThesisNew/thesis/figures/4_evaluation/final", "lo", true);
    }
}