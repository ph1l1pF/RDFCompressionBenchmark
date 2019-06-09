package evaluation;

import Util.Util;
import compressionHandling.*;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.util.*;

public class FinalEvaluator {

    private static final String DIRECTORY = "finalGraph/";
    private static final String FILE_ORIGINAL = DIRECTORY + "mappingbased-properties_en_manyinversesBigger.ttl";
    private static final String FILE_FINAL = DIRECTORY + "final.ttl";
    private static final String FILE_FINAL_ONTOLOGY_MANIPULATED = DIRECTORY + "finalWithAppliedOnt.ttl";

    public static void main(String[] args) {
//        buildGraph();
//        evaluateGraphCompressions(false);

        evaluateCompleteCompressions();
    }

    private static void evaluateCompleteCompressions() {
//        double ratioHDTNormal = evaluateHDT(FILE_FINAL, true, false, false).getCompressionRatio();
//        double ratioHDTImproved = evaluateHDT(FILE_FINAL, true, true, true).getCompressionRatio();

        double dictSizeNormal = new HDTStarter().compress(FILE_FINAL_ONTOLOGY_MANIPULATED, "bla.hdt", false).getCompressedSize();
        System.out.println(new File(FILE_FINAL).length());

//        ZipStarter gzipStarter = new ZipStarter();
//        System.out.println(gzipStarter.compress(FILE_FINAL, "bla.gzp", true).getCompressionRatio());

//        GraphRePairStarter graphRePairStarter = new GraphRePairStarter();
//        double sizeGRPNormal =graphRePairStarter.compress(FILE_FINAL, null, false).getCompressedSize();
//        double sizeGRPOnt =graphRePairStarter.compress(FILE_FINAL_ONTOLOGY_MANIPULATED, null, false).getCompressedSize();


//        System.out.println("hdt normal: " + ratioHDTNormal);
//        System.out.println("hdt improved: " + ratioHDTImproved);
//        System.out.println("grp normal: " + sizeGRPNormal);
//        System.out.println("GRP ont: " + sizeGRPOnt);
    }

    private static void evaluateDictCompressions() {
        HDTStarter hdtStarter = new HDTStarter();
        CompressionResult resultNormal = hdtStarter.compress(FILE_FINAL, "bla.hdt", true);
        hdtStarter.setOmitBlankNodeIds(true);
        hdtStarter.setHuffmanActive(true);
        CompressionResult resultImproved = hdtStarter.compress(FILE_FINAL, "bla.hdt", true);

        System.out.println("Normal: " + resultNormal.getCompressionRatioRalatedToDict());
        System.out.println("Improved: " + resultImproved.getCompressionRatioRalatedToDict());

    }

    private static void evaluateGraphCompressions(boolean ontologyImprovementForGRP) {
        //evaluateGraphCompressions graph compression
        CompressionResult compressionResultHDT = evaluateHDT(FILE_FINAL, false, false, false);

        double ratioGRP = evaluateGRP(FILE_FINAL, false, false, false, ontologyImprovementForGRP);

        System.out.println("HDT: " + compressionResultHDT.getCompressionRatio());
        System.out.println("GRP: " + ratioGRP);
    }

    private static double evaluateGRP(String file, boolean addDict, boolean huffman, boolean omitBlanks, boolean applyOnt) {

        GraphRePairStarter graphRePairStarter = new GraphRePairStarter(huffman, omitBlanks);
        if (!applyOnt) {
            return graphRePairStarter.compress(file, null, addDict).getCompressionRatio();
        } else {
            CompressionResult result = graphRePairStarter.compress(FILE_FINAL_ONTOLOGY_MANIPULATED, null, addDict);
            return 1.0 * result.getCompressedSize() / new File(FILE_FINAL).length();
        }
    }

    private static CompressionResult evaluateHDT(String file, boolean addDict, boolean huffman, boolean omitBlanks) {
        HDTStarter hdtStarter = new HDTStarter(huffman, omitBlanks);
        return hdtStarter.compress(file, "bla.hdt", addDict);
    }

    private static CompressionResult evaluateGZip(String file) {
        GzipStarter gzipStarter = new GzipStarter();
        return gzipStarter.compress(file, null, true);
    }

    private static void buildGraph() {
        Model model = Util.getModelFromFile(FILE_ORIGINAL);
        ExtendedIterator<Triple> iterator = model.getGraph().find();
        Set<String> potentialPersons = new LinkedHashSet<>();
        Set<String> countries = new LinkedHashSet<>();
        final String countryProperty = "http://dbpedia.org/ontology/country";
        while (iterator.hasNext()) {
            Triple triple = iterator.next();

            if (triple.getSubject().isURI()) {
                potentialPersons.add(triple.getSubject().getURI());
            }
            if (triple.getPredicate().getURI().equals(countryProperty)) {
                countries.add(triple.getObject().getURI());
            }
        }

        Map<String, String> abstracts = getAbstracts(potentialPersons);

        int count = 0;
        for (String person : abstracts.keySet()) {
            if (count > abstracts.size() / 2) {
                break;
            }

            String abstrac = abstracts.get(person);
            Triple triple = Triple.create(NodeFactory.createURI(person), NodeFactory.createURI("http://dbpedia.org/ontology/abstract"),
                    NodeFactory.createLiteral(abstrac));
            model.getGraph().add(triple);

            count++;
        }
        System.out.println("literals added: " + count);


        Map<String, List<Node_Blank>> blankNodes = getCoordinates(countries);
        count = 0;
        for (String country : blankNodes.keySet()) {

            for (Node_Blank blank : blankNodes.get(country)) {
                count++;
                Triple triple = Triple.create(NodeFactory.createURI(country), NodeFactory.createURI("http://dbpedia.org/coordinate"),
                        blank);
                model.getGraph().add(triple);
            }

        }

        System.out.println("blank nodes added: " + count);

        Util.writeModelToFile(new File(FILE_FINAL), model);
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

        return mapAbstracts;

    }

    private static Map<String, List<Node_Blank>> getCoordinates(Set<String> places) {

        Map<String, List<Node_Blank>> blanks = new HashMap<>();
        for (String place : places) {
            List<Node_Blank> blankList = new ArrayList<>();
            int numBlanks = Util.getRandomNumberInRange(20, 50);
            for (int i = 0; i < numBlanks; i++) {
                blankList.add((Node_Blank) NodeFactory.createBlankNode());
            }
            blanks.put(place, blankList);
        }

        return blanks;

    }
}