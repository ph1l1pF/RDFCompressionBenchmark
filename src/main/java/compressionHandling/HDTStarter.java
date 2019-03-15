package compressionHandling;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.io.IOException;

public class HDTStarter implements CompressionStarter {

    public CompressionResult compress(String filePath) {

        String baseURI = "http://example.com/mydataset";
        String rdfInput = filePath;
        String inputType = "ntriples";
        String hdtOutput = "dataset.hdt";

        File inputFile = new File(filePath);
        File outputFile = null;


        long compressionTime = -1;
        try {
            outputFile = new File(hdtOutput);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            compressionTime = System.currentTimeMillis();

            HDT hdt = HDTManager.generateHDT(
                    rdfInput,         // Input RDF File
                    baseURI,          // Base URI
                    RDFNotation.parse(inputType), // Input Type
                    new HDTSpecification(),   // HDT Options
                    null              // Progress Listener
            );

            hdt.saveToHDT(hdtOutput, null);
            compressionTime = System.currentTimeMillis() - compressionTime;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        // OPTIONAL: Add additional domain-specific properties to the header:
        //Header header = hdt.getHeader();
        //header.insert("myResource1", "property" , "value");


        return new CompressionResult(inputFile.length(), outputFile.length(), compressionTime, -1, inputFile.getAbsolutePath());
    }

    public CompressionResult decompress(String filePath) {
        return null;
    }

    public static void main(String[] ag) {
        System.out.println(new HDTStarter().compress("/Users/philipfrerk/Documents/RDF_data/Semantic_web_dog_food/eswc-2006-complete.ttl"));
    }
}
