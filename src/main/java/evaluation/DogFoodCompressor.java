package evaluation;

import Util.RDFTurtleConverter;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.base.Sys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DogFoodCompressor {

    public static void main(String[] args) {
        File dir = new File("/Users/philipfrerk/Documents/RDF_data/dbpedia");



        List<File> files = new ArrayList<>();

        List<File> queue = new ArrayList<>();
        queue.add(dir);
        int bound = 50;
        while (!queue.isEmpty() && files.size() <= bound) {
            File currentDir = queue.remove(0);
            for (File file : currentDir.listFiles()) {
                if (file.getAbsolutePath().endsWith("bz2")) {
                    try {
                        files.add(RDFTurtleConverter.convertAndStoreAsTurtleFile(file.getAbsolutePath()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    queue.add(file);
                }
            }
        }

        List<CompressionResult> results = new ArrayList<>();
        CompressionStarter hdtStarter = new GraphRePairStarter();

        for (File file : files) {
            if (file != null)
                results.add(hdtStarter.compress(file.getAbsolutePath(), file.getName() + ".hdt", true));
        }

//        Collections.sort(results);

        for (CompressionResult result : results) {
            if (result != null)
                System.out.print(result.getCompressionRatio() + ", ");
        }
    }
}
