package compressionHandling;

import org.apache.jena.base.Sys;
import org.rdfhdt.hdt.dictionary.impl.BlankAndHuffmanEvaluator;
import org.rdfhdt.hdt.dictionary.impl.HuffmanFacade;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HDTStarter implements CompressionStarter {

    private final Map<String, String> mapSuffixToFormat = new HashMap<>();

    private boolean omitBlankNodeIds;
    private boolean huffmanActive;


    public HDTStarter(boolean omitBlankNodeIds, boolean huffmanActive) {
        this.omitBlankNodeIds = omitBlankNodeIds;
        this.huffmanActive = huffmanActive;
        fillSuffixMap();
    }

    public HDTStarter() {
        omitBlankNodeIds = false;
        huffmanActive = false;
        fillSuffixMap();
    }

    private void fillSuffixMap() {
        final String NTRIPLES = "ntriples";
        final String RDF_XML = "rdf-xml";
        mapSuffixToFormat.put("ttl", NTRIPLES);
        mapSuffixToFormat.put("nt", NTRIPLES);
        mapSuffixToFormat.put("inf", NTRIPLES);
        mapSuffixToFormat.put("rdf", RDF_XML);
    }

    private List<File> getHuffmanOutputFiles() {
        List<File> files = new ArrayList<>();
        files.add(new File(HuffmanFacade.FILE_LITERALS));
        files.add(new File(HuffmanFacade.FILE_TREE));
        files.add(new File(HuffmanFacade.FILE_CHARS));
        return files;
    }

    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        String baseURI = "http://example.com/mydataset";
        String rdfInput = filePath;

        String inputType = mapSuffixToFormat.get(getFileSuffix(filePath));

        if (inputType == null) {
            throw new IllegalArgumentException("Invalid format: " + getFileSuffix(filePath));
        }

        List<File> huffmanOutputFiles = getHuffmanOutputFiles();
        huffmanOutputFiles.forEach(x -> x.delete());

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

            BlankAndHuffmanEvaluator.HUFFMAN_ACTIVE = huffmanActive;
            if (huffmanActive) {
                HuffmanFacade.findCharacterCounts(filePath);
            }

            BlankAndHuffmanEvaluator.BLANK_OMIT_ACTIVE = omitBlankNodeIds;

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

        long compressedSize = outputFile.length();
        outputFile.delete();

        long dictSize = hdt.getDictionary().size();
        compressedSize -= dictSize;

        if (huffmanActive) {
            if (!addDictionarySizeToCompressedSize) {
                throw new RuntimeException("makes no sense");
            }
            for (File huffFile : huffmanOutputFiles) {
                dictSize += huffFile.length();
            }
            huffmanOutputFiles.forEach(x -> x.delete());

        }

        if (addDictionarySizeToCompressedSize) {
            compressedSize += dictSize;
        }

        return new CompressionResult(inputFile.length(), compressedSize, dictSize, compressionTime, -1, inputFile.getAbsolutePath());
    }

    private static String getFileSuffix(String filePath) {
        String[] splitted = filePath.split("\\.");
        return splitted[splitted.length - 1];
    }


    public long decompress(String filePath) {
//        try {
//            HDT hdt = HDTManager.loadHDT("data/example.hdt", null);
//            IteratorTripleID iteratorTripleID = hdt.getTriples().searchAll();
//            while(iteratorTripleID.hasNext()){
//                TripleID next = iteratorTripleID.next();
//                next.
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        throw new RuntimeException("not impl");
    }

    public Map<String, String> getMapSuffixToFormat() {
        return mapSuffixToFormat;
    }

    public boolean isOmitBlankNodeIds() {
        return omitBlankNodeIds;
    }

    public boolean isHuffmanActive() {
        return huffmanActive;
    }

    public void setHuffmanActive(boolean huffmanActive) {
        this.huffmanActive = huffmanActive;
    }

    public void setOmitBlankNodeIds(boolean omitBlankNodeIds) {
        this.omitBlankNodeIds = omitBlankNodeIds;
    }

}
