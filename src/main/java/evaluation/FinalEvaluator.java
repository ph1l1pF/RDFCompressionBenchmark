package evaluation;

import Ontology.DataReplacer;
import Util.Util;
import compressionHandling.*;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rdfhdt.hdt.triples.TriplesFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FinalEvaluator {

    private static final boolean ONTOLOGY_ACTIVE = true;
    private static final boolean HUFFMAN_ACTIVE = true;
    private static final boolean BLANK_OMIT_ACTIVE = true;

    private static final String DIRECTORY = "finalGraph/";
    private static final String FILE_ORIGINAL = DIRECTORY + "mappingbased-properties_en_manyinversesBigger.ttl";
    private static final String FILE_FINAL = DIRECTORY + "final.ttl";
    private static final String FILE_FINAL_ONTOLOGY_MANIPULATED = DIRECTORY + "finalWithAppliedOnt.ttl";


    private static void evaluate() {
        HDTStarter hdtStarter = new HDTStarter();
        GraphRePairStarter grpStarter = new GraphRePairStarter();
        GzipStarter gzipStarter = new GzipStarter();

        final boolean addDictSize = true;
        CompressionResult compressionResultGzip = gzipStarter.compress(FILE_FINAL, "bla.zip", addDictSize);
        CompressionResult compressionResultHDT = hdtStarter.compress(FILE_FINAL, "bla.hdt", addDictSize);
        CompressionResult compressionResultGRP = grpStarter.compress(FILE_FINAL, null, addDictSize);

        hdtStarter.setHuffmanActive(true);
        CompressionResult compressionResultHDTPlusHuffman = hdtStarter.compress(FILE_FINAL, "bla.hdt", addDictSize);

        grpStarter.setHuffmanActive(true);
        CompressionResult compressionResultGRPPlusHuffman = grpStarter.compress(FILE_FINAL,null,addDictSize);

        // ontology
        Model model = Util.getModelFromFile(FILE_FINAL);
        DataReplacer.materializeInverse(DataReplacer.mapDBPediaInversePredicates, model, true);
        Util.writeModelToFile(new File(FILE_FINAL_ONTOLOGY_MANIPULATED), model);


        System.out.println("\n\nResults:\n\n");

        // GRP
        grpStarter.setHuffmanActive(false);
        CompressionResult resultGRPWithOnt = grpStarter.compress(FILE_FINAL_ONTOLOGY_MANIPULATED, null, addDictSize);

        System.out.println("GRP: "+compressionResultGRP.getCompressionRatio());
        System.out.println("GRP + Ont: " + 1.0*resultGRPWithOnt.getCompressedSize() / new File(FILE_FINAL).length());
        //        System.out.println("GRP + Huffman: "+compressionResultGRPPlusHuffman.getCompressionRatio());


//        System.out.println("Gzip: "+compressionResultGzip.getCompressionRatio());
//        System.out.println("HDT: "+compressionResultHDT.getCompressionRatio());
//        System.out.println("HDT + Huffman: "+compressionResultHDTPlusHuffman.getCompressionRatio());
//        System.out.println("GRP:"+compressionResultGRP.getCompressionRatio());
//        System.out.println("GRP + Huffman:"+compressionResultGRPPlusHuffman.getCompressionRatio());

    }

    private static void buildGraph() {
        Model model = Util.getModelFromFile(FILE_ORIGINAL);
        ExtendedIterator<Triple> iterator = model.getGraph().find();
        Set<String> potentialPersons = new LinkedHashSet<>();
        Set<String> birthplaces = new LinkedHashSet<>();
        final String birthplaceProperty = "";
        while (iterator.hasNext()) {
            Triple triple = iterator.next();

            if (triple.getSubject().isURI()) {
                potentialPersons.add(triple.getSubject().getURI());
            }
            if (triple.getPredicate().getURI().equals(birthplaceProperty)) {
                birthplaces.add(triple.getPredicate().getURI());
            }
        }

        Map<String, String> abstracts = getAbstracts(potentialPersons);
//        Map<String, String> blankNodes = getCoordinates(birthplaces);

        int count =0;
        for (String person : abstracts.keySet()) {
            if(count>abstracts.size()/2){
                break;
            }

            String abstrac = abstracts.get(person);
            Triple triple = Triple.create(NodeFactory.createURI(person), NodeFactory.createURI("http://dbpedia.org/ontology/abstract"),
                    NodeFactory.createLiteral(abstrac));
            model.getGraph().add(triple);

            count++;
        }

        //TODO das gleiche für blankNodes

        Util.writeModelToFile(new File(FILE_FINAL), model);
    }


    public static void main(String[] args) {
//        FileReader fi = null;
//        BufferedReader bufferedReader = null;
//        try {
//
//            fi = new FileReader("mappingbased-properties_en_manyinverses.ttl");
//            bufferedReader = new BufferedReader(fi);
//            String line = bufferedReader.readLine();
//
//            line = null;
//            while (line != null) {
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                bufferedReader.close();
//                fi.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        buildGraph();
evaluate();

    }

    private static Map<String, String> getAbstracts(Set<String> persons) {
        Model m = Util.getModelFromFile("/Users/philipfrerk/Downloads/long-abstracts-en-uris_bg.nt");
        ExtendedIterator<Triple> iterator = m.getGraph().find();

        Map<String, String> mapAbstracts = new LinkedHashMap<>();

        while (iterator.hasNext()) {
            Triple triple = iterator.next();
            if (triple.getSubject().isURI()) {
                for (String person : persons) {
                    if (person.equals(triple.getSubject().getURI())) {
                        mapAbstracts.put(person, triple.getObject().getLiteral().toString());
                        break;
                    }
                }
            }
        }

        System.out.println("abstracts: " + mapAbstracts.size());
        return mapAbstracts;

    }

    private static Map<String, String> getCoordinates(Set<String> places) {

        throw new RuntimeException();
    }
}