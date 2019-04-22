package Ontology;

import org.apache.jena.rdf.model.Model;

import java.util.*;

public class DataReplacer {

    private static final Map<String, WikiDataHandler.Result> mapPredToResult = new LinkedHashMap<>();


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

    public static void getWikiResults(Model ontologyDBPedia){
        // find equivalent wikidata-predicates
        LinkedHashMap<String, List<String>> map = QueryExecutor.getAllPredicateEuivClassesWithBinaryProperty(ontologyDBPedia, OntologyEvaluator.EUIVALENT_PROPERTIES);
        LinkedHashMap<String, String> mapDBPediaToWikiData = new LinkedHashMap<>();


        outer :for(String pred : map.keySet()){
            for(String pred2 : map.get(pred)){
                if(pred2.contains("http://www.wikidata.org")){
                    mapDBPediaToWikiData.put(pred,pred2);
                    continue outer;
                }
            }
        }

        for(String dbPediaPredicate : mapDBPediaToWikiData.keySet()){
            String wikiDataPredicate  = mapDBPediaToWikiData.get(dbPediaPredicate);

            // check if the wikiDataPredicate is symmetric
            WikiDataHandler.Result result = WikiDataHandler.getResultForPredicate(dbPediaPredicate, wikiDataPredicate);
            mapPredToResult.put(dbPediaPredicate, result);
        }

    }

    public static int materializeAllSymmetricDBPediaPredicates(Model model, Model ontologyDBPedia) {


        List<String> symmetricPredicates = new ArrayList<>();
        for(String pred : mapPredToResult.keySet()){
            if(mapPredToResult.get(pred)!=null && mapPredToResult.get(pred).isSymmetric()){
                symmetricPredicates.add(pred);
            }
        }

        return materializeSymmetry(symmetricPredicates,  model);
    }

    public static int materializeSymmetry(List<String> symmetricPredicates, Model model){
        int count = 0;
        for (String pred : symmetricPredicates) {

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


    public static int dematerializeAllTransitivePredicates(Model model) {

        List<String> transPredicates = new ArrayList<>();
        for(String pred : mapPredToResult.keySet()){
            if(mapPredToResult.get(pred)!=null && mapPredToResult.get(pred).isTransitive()){
                transPredicates.add(pred);
            }
        }

        //TODO: not working yet
        for (String pred : transPredicates) {
            String sparql = "DELETE { ?s <" + pred + "> ?o}\n" +
                    "WHERE { \n" +
                    "?s <" + pred + ">{2,} ?o.\n"+
                    "?s <"+pred+"> ?o }";

//            String sparql = "DELETE { ?s ?p ?o}\n" +
//                    "WHERE { \n" +
//                    "?s ?p{2,} ?o\n"+
//            "FILTER (?p = <" + pred + ">) \n}";


            QueryExecutor.executeSparql(model, sparql, false);
        }

        return 0;
    }

    public static void main(String[] args) {
        Model m = Util.Util.getModelFromFile("file.ttl", 0.01);
        Model ontology = Util.Util.getModelFromFile("dbpedia_2015-04.owl");

        getWikiResults(ontology);

//        List<String> p = new ArrayList<>();
//        p.add("http://5");
        materializeAllSymmetricDBPediaPredicates(m,ontology);
        dematerializeAllTransitivePredicates(m);
//        dematerializeAllTransitivePredicates(m,p);

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
