package compressionHandling;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStarter{


    public CompressionResult compress(String[] filePaths, String outputName, boolean addDictionarySizeToCompressedSize) {

        long origSize = 0;
        for(String f : filePaths){
            origSize+=new File(f).length();
        }


        try {
            // create byte buffer
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(outputName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (int i=0; i < filePaths.length; i++) {
                File srcFile = new File(filePaths[i]);
                FileInputStream fis = new FileInputStream(srcFile);
                // begin writing a new ZIP entry, positions the stream to the start of the entry data
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int length;

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();

                // close the InputStream
                fis.close();

            }

            // close the ZipOutputStream
            zos.close();

            return new CompressionResult(origSize, new File(outputName).length(),-1,-1,-1,null);

        }
        catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }
        return null;
    }


}
