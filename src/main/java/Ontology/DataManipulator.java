package Ontology;

import Util.Util;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataManipulator {

    public static void storeSubModel(String origModelPath, String newModelPath, List<String> desiredStrings, int numDesired, int numResidual){
        Model newModel = Util.streamRealSubModelFromFile(origModelPath, numDesired, numResidual, desiredStrings);
        Util.writeModelToFile(new File(newModelPath), newModel);
    }

    public static void shortenModel(String fileOld, String fileNew){
        Model model = Util.streamModelFromFile(fileOld, Util.TRIPLE_AMOUNT);
        Util.writeModelToFile(new File(fileNew),model);
    }

    public static void main (String[] args) throws IOException {

        List<String> symmetricPredicatesWordnet = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/symmetricProperties"));

//        storeSubModel("wordnet.nt", "wordnet_withmanysymmetrics.ttl", symmetricPredicatesWordnet, 1000, 1000);

        List<String> inversePredicatesWordnet = new ArrayList<>();
        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hypernym");
        inversePredicatesWordnet.add("http://wordnet-rdf.princeton.edu/ontology#hyponym");


//        storeSubModel("wordnet.nt", "wordnet_withmanyinverse.ttl", inversePredicatesWordnet, 1000, 1000);

        List<String> symmetricPredicatesDbPedia = new ArrayList<>();
        List<String> transitivePredicatesDbPedia = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/DBPedia_Relevant_Data/transitiveProperties.txt"));

        symmetricPredicatesDbPedia.add("http://dbpedia.org/ontology/spouse");
//        storeSubModel("mappingbased-properties_en.ttl", "mappingbased-properties_en_manytransitives.ttl",
//                transitivePredicatesDbPedia,1000,1000);

        Model modelFromFile = Util.getModelFromFile("mappingbased-properties_en_manytransitives.ttl");
        DataReplacer.dematerializeTransitive(transitivePredicatesDbPedia,modelFromFile,true);
        Util.writeModelToFile(new File("mappingbased-properties_en_manytransitives_manipulated.ttl"),modelFromFile);

    }
}
