package Ontology;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.util.*;

public class DataReplacer {

    public static int replaceAllEquivalentPredicates(Model model, Map<String, List<String>> euivalenceMapping) {

        int count = 0;
        for (String strKey : euivalenceMapping.keySet()) {
            for (String strValue : euivalenceMapping.get(strKey)) {

                String sparql = "SELECT ?s ?p ?o\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <" + strValue + ">) \n" +
                        "}";

                count += QueryExecutor.getCount(model, sparql);


                sparql = "DELETE {?s ?p ?o}\n" +
                        "INSERT {?s <" + strKey + "> ?o" +
                        "       }\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <" + strValue + ">) \n" +
                        "}";
                QueryExecutor.executeSparql(model, sparql, false);
            }
        }

        return count;
    }

    public static int materializeAllSymmetricPredicates(Model model, List<String> predicates) {
        // find equivalent wikidata-predicates
        LinkedHashMap<String, List<String>> map = QueryExecutor.getAllPredicateEuivClassesWithBinaryProperty(model, "");
        LinkedHashMap<String, String> mapDBPediaToWikiData = new LinkedHashMap<>();


        outer :for(String pred : map.keySet()){
            for(String pred2 : map.get(pred)){
                if(pred2.toString().contains("wikidata")){ //TODO: passt das?
                    mapDBPediaToWikiData.put(pred,pred2);
                    continue outer;
                }
            }
        }

        for(String dbPediaPredicate : mapDBPediaToWikiData.keySet()){
            String wikiDataPredicate  = mapDBPediaToWikiData.get(dbPediaPredicate);

            // check if the wikiDataPredicate is symmetric
            String sparql = "";
            Model wikiDataOnt = Util.Util.getModelFromFile("");
            QueryExecutor.executeSparql(wikiDataOnt, sparql,true);


        }


        int count = 0;
        for (String pred : predicates) {

            String sparql = "SELECT ?s ?p ?o {\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + pred + "> ?s }\n" +
                    "}";

            count += QueryExecutor.getCount(model, sparql);


            sparql = "insert {?o <" + pred + "> ?s}\n" +
                    "    WHERE { ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + pred + "> ?s }\n" +
                    "}";

            QueryExecutor.executeSparql(model, sparql, false);


        }
        return count;
    }


    public static int dematerializeAllTransitivePredicates(Model model, List<String> predicates) {
        //TODO: not working yet
        for (String pred : predicates) {
//            String sparql = "DELETE { ?s <" + pred + "> ?o}\n" +
//                    "WHERE { \n" +
//                    "?s <" + pred + ">{2,} ?o.\n}";
////                    "?s <"+pred+"> ?o }";

            String sparql = "DELETE { ?s ?p ?o}\n" +
                    "WHERE { \n" +
                    "?s ?p{2,} ?o\n"+
            "FILTER (?p = <" + pred + ">) \n}";


            QueryExecutor.executeSparql(model, sparql, false);
        }

        return 0;
    }

    public static void main(String[] args) {
        Model m = Util.Util.getModelFromFile("testfile.ttl");

        Util.Util.printModel(m);

        List<String> p = new ArrayList<>();
        p.add("http://5");
//        materializeAllSymmetricPredicates(m,p);
        dematerializeAllTransitivePredicates(m,p);

//
//        Map<String, List<String>> euivalenceMapping = new HashMap<>();
//        String key = "http://5";
//        List<String> value = new ArrayList<>();
//        value.add("http://6");
//        euivalenceMapping.put(key, value);
//
//        replaceAllEquivalentPredicates(m, euivalenceMapping);
//        Util.Util.printModel(m);

    }


}
