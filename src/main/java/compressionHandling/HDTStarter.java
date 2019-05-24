package compressionHandling;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HDTStarter implements CompressionStarter {

    private final Map<String, String> mapSuffixToFormat = new HashMap<>();

    public HDTStarter() {
        final String NTRIPLES = "ntriples";
        final String RDF_XML = "rdf-xml";
        mapSuffixToFormat.put("ttl", NTRIPLES);
        mapSuffixToFormat.put("nt", NTRIPLES);
        mapSuffixToFormat.put("inf", NTRIPLES);
        mapSuffixToFormat.put("rdf", RDF_XML);
    }

    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        String baseURI = "http://example.com/mydataset";
        String rdfInput = filePath;

        String inputType = mapSuffixToFormat.get(getFileSuffix(filePath));

        if (inputType == null) {
            throw new IllegalArgumentException("Invalid format: " + getFileSuffix(filePath));
        }

        File inputFile = new File(filePath);
        File outputFile = null;

        long compressionTime = -1;
        HDT hdt = null;
        try {
            outputFile = new File(outputName);
            if (outputFile.exists()) {
                outputFile.delete();
            }

            compressionTime = System.currentTimeMillis();

            hdt = HDTManager.generateHDT(
                    rdfInput,         // Input RDF File
                    baseURI,          // Base URI
                    RDFNotation.parse(inputType), // Input Type
                    new HDTSpecification(),   // HDT Options
                    null              // Progress Listener
            );

            hdt.saveToHDT(outputName, null);
            compressionTime = System.currentTimeMillis() - compressionTime;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        // OPTIONAL: Add additional domain-specific properties to the header:
        //Header header = hdt.getHeader();
        //header.insert("myResource1", "property" , "value");

        long compressedSize = outputFile.length();
        if (!addDictionarySizeToCompressedSize) {
            compressedSize -= hdt.getDictionary().size();
        }

        return new CompressionResult(inputFile.length(), compressedSize, hdt.getDictionary().size(), compressionTime, -1, inputFile.getAbsolutePath());
    }

    private static String getFileSuffix(String filePath) {
        String[] splitted = filePath.split("\\.");
        return splitted[splitted.length - 1];
    }


    public long decompress(String filePath) {
        return 0;
    }
}
