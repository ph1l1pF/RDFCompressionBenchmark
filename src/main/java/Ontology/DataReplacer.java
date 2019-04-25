package Ontology;

import Util.Triple;
import Util.Util;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.*;

public class DataReplacer {

    private static Map<String, WikiDataHandler.Result> mapPredToResult = new HashMap<>();


    public static int replaceAllEquivalentPredicates(Model model, Map<String, List<String>> euivalenceMapping) {

        int count = 0;
        for (String strKey : euivalenceMapping.keySet()) {
            for (String strValue : euivalenceMapping.get(strKey)) {

                String sparql = "SELECT ?s ?p ?o\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <" + strValue + ">) \n" +
                        "}";

                count += SparqlExecutor.getCount(model, sparql);


                sparql = "DELETE {?s ?p ?o}\n" +
                        "INSERT {?s <" + strKey + "> ?o" +
                        "       }\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <" + strValue + ">) \n" +
                        "}";
                SparqlExecutor.executeSparql(model, sparql, false);
            }
        }

        return count;
    }

    public static void getWikiResults(String ontologyDBPedia) {

        String fileName = "wiki-" + ontologyDBPedia + ".ser";
        File currentDir = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());
        boolean alreadyExistis = false;
        for (File file : currentDir.listFiles()) {
            if (file.getName().equals(fileName)) {
                alreadyExistis = true;
                break;
            }
        }

        if (!alreadyExistis) {


            // find equivalent wikidata-predicates
            LinkedHashMap<String, List<String>> map = SparqlExecutor.getAllPredicateEuivClassesWithBinaryProperty(Util.getModelFromFile(ontologyDBPedia), OntologyEvaluator.EUIVALENT_PROPERTIES);
            LinkedHashMap<String, List<String>> mapDBPediaToWikiData = new LinkedHashMap<>();


            outer:
            for (String pred : map.keySet()) {
                for (String pred2 : map.get(pred)) {
                    if (pred2.contains("http://www.wikidata.org")) {

                        List<String> values;
                        if (mapDBPediaToWikiData.containsKey(pred)) {
                            values = mapDBPediaToWikiData.get(pred);
                        } else {
                            values = new ArrayList<>();
                        }
                        values.add(pred2);

                        mapDBPediaToWikiData.put(pred, values);
                        continue outer;
                    }
                }
            }

            for (String dbPediaPredicate : mapDBPediaToWikiData.keySet()) {
                for (String wikiDataPredicate : mapDBPediaToWikiData.get(dbPediaPredicate)) {

                    // check if the wikiDataPredicate is symmetric
                    WikiDataHandler.Result result = WikiDataHandler.getResultForPredicate(dbPediaPredicate, wikiDataPredicate);
                    mapPredToResult.put(dbPediaPredicate, result);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(fileName);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(mapPredToResult);
            } catch (IOException e) {

            }

        } else {
            try (FileInputStream fis = new FileInputStream(fileName);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                mapPredToResult = (Map) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
            }
        }

        for (String s : mapPredToResult.keySet()) {
            if (mapPredToResult.get(s) != null && !mapPredToResult.get(s).getInverseProperties().isEmpty()) {
                System.out.println(s + " : " + mapPredToResult.get(s).getInverseProperties());
            }
        }
    }

    public static int materializeAllSymmetricDBPediaPredicates(Model model, Model ontologyDBPedia) {


        List<String> symmetricPredicates = new ArrayList<>();
        for (String pred : mapPredToResult.keySet()) {
            if (mapPredToResult.get(pred) != null && mapPredToResult.get(pred).isSymmetric()) {
                symmetricPredicates.add(pred);
            }
        }

        return materializeSymmetry(symmetricPredicates, model);
    }

    public static int materializeAllInverseDBPediaPredicates(Model model, Model ontologyDBPedia) {


        Map<String, String> invPredicates = new HashMap<>();

        LinkedHashMap<String, List<String>> euivalentProperties = OntologyEvaluator.getAllEuivalentProperties(ontologyDBPedia);

        for (String predDBPedia : mapPredToResult.keySet()) {
            if (mapPredToResult.get(predDBPedia) == null) {
                continue;
            }


            String dbPediaInverse = null;
            for (String invPredWiki : mapPredToResult.get(predDBPedia).getInverseProperties()) {
                if(findDBPredicateForWikiPredicate(invPredWiki,euivalentProperties)!=null){
                    dbPediaInverse = findDBPredicateForWikiPredicate(invPredWiki,euivalentProperties);
                    break;
                }
            }
            if(dbPediaInverse!=null){
                invPredicates.put(predDBPedia, dbPediaInverse);
            }

        }

        return materializeInverse(invPredicates, model);
    }


    private static String findDBPredicateForWikiPredicate(String wikiPred, LinkedHashMap<String, List<String>> euivalentProperties) {
        for (String dbPred : euivalentProperties.keySet()) {
            for (String wikiPr : euivalentProperties.get(dbPred)) {
                if (wikiPr.equals(wikiPred)) {
                    return dbPred;
                }
            }
        }

        return null;
    }

    private static int materializeInverse(Map<String, String> invPredicates, Model model) {
        int count = 0;
        for (String pred : invPredicates.keySet()) {

            String predInv = invPredicates.get(pred);

            String sparql = "SELECT ?s ?p ?o {\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + predInv + "> ?s }\n" +
                    "}";

            count += SparqlExecutor.getCount(model, sparql);


            sparql = "insert {?o <" + predInv + "> ?s}\n" +
                    "    WHERE { ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + predInv + "> ?s }\n" +
                    "}";

            SparqlExecutor.executeSparql(model, sparql, false);
        }
        return count;
    }

    public static int materializeSymmetry(List<String> symmetricPredicates, Model model) {
        int count = 0;
        for (String pred : symmetricPredicates) {

            String sparql = "SELECT ?s ?p ?o {\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + pred + "> ?s }\n" +
                    "}";

            count += SparqlExecutor.getCount(model, sparql);


            sparql = "insert {?o <" + pred + "> ?s}\n" +
                    "    WHERE { ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + pred + "> ?s }\n" +
                    "}";

            SparqlExecutor.executeSparql(model, sparql, false);
        }
        return count;
    }


    public static int dematerializeAllTransitivePredicates(Model model) {

        List<String> transPredicates = new ArrayList<>();
        for (String pred : mapPredToResult.keySet()) {
            if (mapPredToResult.get(pred) != null && mapPredToResult.get(pred).isTransitive()) {
                transPredicates.add(pred);
            }
        }

        return dematerializeTransitive(transPredicates, model);
    }
//
//    private static String getLabel(QuerySolution querySolution, String s) {
//        if (querySolution.get(s).isURIResource()) {
//            return "<" + querySolution.getResource(s) + ">";
//        } else if (querySolution.get(s).isLiteral()) {
//            return "\"" + querySolution.getLiteral(s) + "\"";
//        } else {
//            throw new RuntimeException("strange node: " + querySolution);
//        }
//    }

    private static int dematerializeTransitive(List<String> transPredicates, Model model) {
        int count = 0;
        for (String pred : transPredicates) {
            String sparql = "select ?s ?p ?o\n" +
                    "WHERE { \n" +
                    "?s <" + pred + ">/<" + pred + ">+ ?o.\n" +
                    "?s <" + pred + "> ?o }";

            count += SparqlExecutor.getCount(model, sparql);

            sparql = "delete { ?s <" + pred + "> ?o }\n" +
                    "WHERE { \n" +
                    "?s <" + pred + ">/<" + pred + ">+ ?o.\n" +
                    "?s <" + pred + "> ?o }";

            SparqlExecutor.executeSparql(model, sparql, false);

        }
        return count;
    }

    public static void main(String[] args) {
        Model m = Util.getModelFromFile("testFile.ttl");
        Model ontology = Util.getModelFromFile("dbpedia_2015-04.owl");

        getWikiResults("dbpedia_2016-10.owl");

//        List<String> p = new ArrayList<>();
//        p.add("http://5");
//        materializeAllSymmetricDBPediaPredicates(m,ontology);
//        dematerializeAllTransitivePredicates(m);
//        dematerializeAllTransitivePredicates(m,p);

//        Map<String,String> invPred = new HashMap<>();
//        invPred.put("http://n1","http://i1");
//        invPred.put("http://n2","http://i2");

//        List<String> tran = new ArrayList<>();
//        tran.add("http://n1");
//
//        Util.Util.printModel(m);
//        System.out.println(dematerializeTransitive(tran, m));
//        Util.Util.printModel(m);


        materializeAllInverseDBPediaPredicates(m,ontology);


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
