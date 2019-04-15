package Ontology;

import org.apache.jena.rdf.model.Model;

import java.util.List;
import java.util.Map;

public class DataReplacer {

    public static void replaceAllEquivalentPredicates(Model model, Map<String, List<String>> euivalenceMapping){
        for(String strKey: euivalenceMapping.keySet()){
            for(String strValue : euivalenceMapping.get(strKey)){
                String sparql = "DELETE {?s ?p ?o}\n" +
                        "INSERT {?s < "+strKey + ">\n" +
                        "       }\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <"+strValue+">) \n" +
                        "}";
                QueryExecutor.executeSparql(model,sparql,false);
            }
        }
    }

    public static void materializeAllSymmetricPredicates(Model model, List<String> predicates){
        throw new RuntimeException();
    }

    public static void dematerializeAllTransitivePredicates(Model model, List<String> predicates){
        throw new RuntimeException();
    }
}
