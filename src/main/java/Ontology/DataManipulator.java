package Ontology;

import Util.Util;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManipulator {

    public static void storeSubModel(String origModelPath, String newModelPath, List<String> desiredStrings, int numDesired, int numResidual){
        Model newModel = Util.streamRealSubModelFromFile(origModelPath, numDesired, numResidual, desiredStrings);
        Util.writeModelToFile(new File(newModelPath), newModel);
    }

    public static void shortenModel(String fileOld, String fileNew){
        Model model = Util.streamModelFromFile(fileOld, Util.TRIPLE_AMOUNT);
        Util.writeModelToFile(new File(fileNew),model);
    }

    private static List<String> getInversePropertiesFromFile(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file));
        Set<String> props = new HashSet<>();
        for(String line : lines){
            String[] split = line.split(" ");
            if(split.length>2){
                System.out.println("nooo");
                return null;
            }
            props.add(split[0]);
            props.add(split[1]);
        }
        return new ArrayList<>(props);
    }

    public static void main (String[] args) throws IOException {

        List<String> symmetricPredicatesWordnet = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/symmetricProperties"));
//
        storeSubModel("wordnet.nt", "wordnet_withmanysymmetrics.ttl", symmetricPredicatesWordnet, 1000, 1000);

//        List<String> inversePredicatesWordnet = new ArrayList<>();
//        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hypernym");
//        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hyponym");


//        storeSubModel("wordnet.nt", "wordnet_withmanyinverse.ttl", inversePredicatesWordnet, 1000, 1000);

        List<String> symmetricPredicatesDbPedia = new ArrayList<>();
        List<String> transitivePredicatesDbPedia = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/transitiveProperties.txt"));

//        symmetricPredicatesDbPedia.add("http://dbpedia.org/ontology/spouse");
//        storeSubModel("mappingbased-properties_en.ttl", "mappingbased-properties_en_manysymmetrics.ttl",
//                symmetricPredicatesDbPedia,10000,10000);

//        Model modelFromFile = Util.getModelFromFile("mappingbased-properties_en_manytransitives.ttl");
//        DataReplacer.dematerializeTransitive(transitivePredicatesDbPedia,modelFromFile,true);
//        Util.writeModelToFile(new File("mappingbased-properties_en_manytransitives_manipulated.ttl"),modelFromFile);

//        List<String> inversePropertiesDBPedia = getInversePropertiesFromFile("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/inverseProperties.txt");
//        storeSubModel("mappingbased-properties_en.ttl", "mappingbased-properties_en_manyinverses.ttl", inversePropertiesDBPedia, 1000, 100);

    }
}
