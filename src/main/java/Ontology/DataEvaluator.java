package Ontology;

import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataEvaluator {

    public static long countTriplesWithPredicates(Model data, List<String> predicates){
        long numTriples = 0;
        for(String predicate : predicates){
            numTriples+=QueryExecutor.countTriplesContainingPredicate(data,predicate);
        }
        return numTriples;
    }


    public static Map<String, Long> countTriplesWithPredicates(List<String> dataFiles, List<String> predicates){
        Map<String, Long> fileToTriplesAmount = new LinkedHashMap<>();
        for(String filePath : dataFiles){
            long amount = countTriplesWithPredicates(Util.Util.getModelFromFile(filePath),predicates);
            fileToTriplesAmount.put(filePath, amount);
        }
        return fileToTriplesAmount;
    }

}
