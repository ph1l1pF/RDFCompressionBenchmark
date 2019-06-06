package Ontology;

import Util.Util;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataManipulator {

    private static List<String> transitivePredicatesWordnet;
    private static List<String> symmericPredicatesWordnet;
    private static List<String> symmericPredicatesDBPedia;

    private static Map<String,String> inversePredicatesDBPedia = new HashMap<>();
    private static Map<String,String> inversePredicatesWordnet = new HashMap<>();


    static {
        try {
            transitivePredicatesWordnet = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/transitiveProperties"));
            symmericPredicatesWordnet = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/symmetricProperties"));
            symmericPredicatesDBPedia = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/symmetricPorperties.txt"));

            inversePredicatesDBPedia = getInversePropertiesFromFile("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/inverseProperties.txt");
            inversePredicatesWordnet = getInversePropertiesFromFile("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/inverseProperties");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeSubModel(String origModelPath, String newModelPath, List<String> desiredStrings, int numDesired, int numResidual){
        Model newModel = Util.streamRealSubModelFromFile(origModelPath, numDesired, numResidual, desiredStrings);
        Util.writeModelToFile(new File(newModelPath), newModel);
    }

    public static void shortenModel(String fileOld, String fileNew){
        Model model = Util.streamModelFromFile(fileOld, Util.TRIPLE_AMOUNT);
        Util.writeModelToFile(new File(fileNew),model);
    }

    private static Map<String,String> getInversePropertiesFromFile(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file));
        Map<String,String> props = new HashMap<>();
        for(String line : lines){
            String[] split = line.split(" ");
            if(split.length>2){
                System.out.println("nooo");
                return null;
            }
            props.put(split[0],split[1]);
        }
        return props;
    }

    private static void createGraphWithHalfEdgesAdded(String feature, String fileIn, String fileOut, CompressionEvaluator.Dataset dataset) throws IOException {
        Model wholeModel = Util.getModelFromFile(fileIn);

        ExtendedIterator<Triple> iterator = wholeModel.getGraph().find();
        Model modelFirstHalf = ModelFactory.createDefaultModel();
        Model modelSecondHalf = ModelFactory.createDefaultModel();

        int size = wholeModel.getGraph().size();
        int count = 0;
        while(iterator.hasNext()){
            Triple next = iterator.next();
            if(count<size/2){
                modelFirstHalf.getGraph().add(next);
            }else{
                modelSecondHalf.getGraph().add(next);
            }
            count++;
        }

        if(feature.equals(CompressionEvaluator.TRANSITIVE)) {
            if(dataset==CompressionEvaluator.Dataset.WORDNET) {
                DataReplacer.dematerializeTransitive(transitivePredicatesWordnet, modelFirstHalf, true);
            }if(dataset==CompressionEvaluator.Dataset.DB_PEDIA) {
                throw new RuntimeException("nooo");
            }
        }else if(feature.equals(CompressionEvaluator.SYMMETRIC)){
            if(dataset==CompressionEvaluator.Dataset.WORDNET) {
                DataReplacer.materializeSymmetry(symmericPredicatesWordnet, modelFirstHalf, true);
            }if(dataset==CompressionEvaluator.Dataset.DB_PEDIA) {
                DataReplacer.materializeSymmetry(symmericPredicatesDBPedia, modelFirstHalf, true);
            }
        }else if(feature.equals(CompressionEvaluator.INVERSE)){
            if(dataset==CompressionEvaluator.Dataset.WORDNET) {
                DataReplacer.materializeInverse(inversePredicatesDBPedia, modelFirstHalf, true);
            }if(dataset==CompressionEvaluator.Dataset.DB_PEDIA) {
                DataReplacer.materializeSymmetry(symmericPredicatesDBPedia, modelFirstHalf, true);
            }
        }

        Model finalModel = ModelFactory.createDefaultModel();
        finalModel.add(modelFirstHalf);
        finalModel.add(modelSecondHalf);

        Util.writeModelToFile(new File(fileOut), finalModel);
    }

    public static void main (String[] args) throws IOException {

//        createGraphWithHalfEdgesAdded(CompressionEvaluator.SYMMETRIC, "mappingbased-properties_en_manysymmetrics.ttl",
//                "mappingbased-properties_en_manysymmetricshalfEdgesAdded.ttl", CompressionEvaluator.Dataset.DB_PEDIA);

//        List<String> symmetricPredicatesWordnet = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/symmetricProperties"));
//
//        storeSubModel("wordnet.nt", "wordnet_withmanytransitives.ttl", transitivePredicatesWordnet, 2000, 1000);

//        List<String> inversePredicatesWordnet = new ArrayList<>();
//        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hypernym");
//        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hyponym");


//        storeSubModel("wordnet.nt", "wordnet_withmanyinverse.ttl", inversePredicatesWordnet, 1000, 1000);

//        List<String> symmetricPredicatesDbPedia = new ArrayList<>();
//        List<String> transitivePredicatesDbPedia = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/transitiveProperties.txt"));

//        symmetricPredicatesDbPedia.add("http://dbpedia.org/ontology/spouse");
//        storeSubModel("mappingbased-properties_en.ttl", "mappingbased-properties_en_manysymmetrics.ttl",
//                symmetricPredicatesDbPedia,10000,10000);


//        Model modelFromFile = Util.getModelFromFile("mappingbased-properties_en_manyinverses.ttl");
//        DataReplacer.materializeInverse(inversePredicatesDBPedia,modelFromFile,false);
//        Util.writeModelToFile(new File("mappingbased-properties_en_manyinverses_alledgesremoved.ttl"),modelFromFile);

        storeSubModel("mappingbased-properties_en.ttl", "finalGraph/mappingbased-properties_en_manyinversesBigger.ttl", DataReplacer.lstDBPediaInversePredicates, 50000, 10000);

    }
}
