package Ontology;

import Util.Util;
import org.apache.jena.rdf.model.Model;

import java.util.*;

public class DataEvaluator {

    public static long countTriplesWithPredicates(Model data, List<String> predicates){
        long numTriples = 0;
        for(String predicate : predicates){
            numTriples+=SparqlExecutor.countTriplesContainingPredicate(data,predicate);
        }
        return numTriples;
    }


    public static Map<String, Long> countTriplesWithPredicates(List<String> dataFiles, List<String> predicates){
        Map<String, Long> fileToTriplesAmount = new LinkedHashMap<>();
        for(String filePath : dataFiles){
            long amount = countTriplesWithPredicates(Util.getModelFromFile(filePath),predicates);
            fileToTriplesAmount.put(filePath, amount);
        }
        return fileToTriplesAmount;
    }


    public static void main(String[] args){
        LinkedHashMap<String, List<String>> euivalentProperties = OntologyEvaluator.getAllEuivalentProperties(Util.getModelFromFile("dbpedia_2015-04.owl"));
        Set<String> predicates = new HashSet<>();

        predicates.addAll(euivalentProperties.keySet());
        for(List<String> value : euivalentProperties.values()){
            predicates.addAll(value);
        }

        List<String> dataFiles = new ArrayList<>();
        dataFiles.add("dbpedia2015/persondata_en.nt");
        Map<String, Long> map = countTriplesWithPredicates(dataFiles, new ArrayList<String>(predicates));

        System.out.println(map);

    }
}
