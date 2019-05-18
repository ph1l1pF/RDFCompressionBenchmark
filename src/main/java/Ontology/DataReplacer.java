package Ontology;

import Util.Util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataReplacer {

    private static Map<String, WikiDataHandler.Result> mapWikiPredToResult = new HashMap<>();

    private static List<String> lstWordnetTransitivePredicates = new ArrayList<>();
    private static List<String> lstWordnetSymmetricPredicates = new ArrayList<>();
    private static Map<String,String> mapWordnetInversePredicates = new HashMap<>();


    static {

        getWikiResults("dbpedia_2015-04.owl");

        try {
            lstWordnetTransitivePredicates= Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/transitiveProperties"), Charset.defaultCharset());
            lstWordnetSymmetricPredicates= Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/symmetricProperties"), Charset.defaultCharset());

            List<String> inverseLines = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/inverseProperties"), Charset.defaultCharset());
            for(String inverseLine : inverseLines){
                String[] uris = inverseLine.split(" ");
                mapWordnetInversePredicates.put(uris[0].trim(),uris[1].trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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

    private static void  getWikiResults(String ontologyDBPedia) {

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
                    mapWikiPredToResult.put(dbPediaPredicate, result);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(fileName);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(mapWikiPredToResult);
            } catch (IOException e) {

            }

        } else {
            try (FileInputStream fis = new FileInputStream(fileName);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                mapWikiPredToResult = (Map) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
            }
        }
    }

    public static int materializeAllSymmetricDBPediaPredicates(Model model, Model ontologyDBPedia, boolean addEdge, final List<String> relevantOntologyTriples) {

        List<String> symmetricPredicates = new ArrayList<>();
        if(CompressionEvaluator.dataset==CompressionEvaluator.Dataset.DB_PEDIA) {
            for (String pred : mapWikiPredToResult.keySet()) {
                if (mapWikiPredToResult.get(pred) != null && mapWikiPredToResult.get(pred).isSymmetric()) {
                    symmetricPredicates.add(pred);
                }
            }
        }
        if(CompressionEvaluator.dataset==CompressionEvaluator.Dataset.WORDNET){
            symmetricPredicates = lstWordnetSymmetricPredicates;
        }

        for(String pred : symmetricPredicates){
            relevantOntologyTriples.add("<"+pred+"> <rdf:type> <owl:SymmetricProperty> .");
        }

        return materializeSymmetry(symmetricPredicates, model, addEdge);
    }

    public static int materializeAllInverseDBPediaPredicates(Model model, Model ontologyDBPedia, boolean addEdges, final List<String> relevantOntologyTriples) {

        Map<String, String> invPredicates = new HashMap<>();

        if(CompressionEvaluator.dataset==CompressionEvaluator.Dataset.DB_PEDIA) {
            LinkedHashMap<String, List<String>> euivalentProperties = OntologyEvaluator.getAllEuivalentProperties(ontologyDBPedia);

            for (String predDBPedia : mapWikiPredToResult.keySet()) {
                if (mapWikiPredToResult.get(predDBPedia) == null) {
                    continue;
                }


                String dbPediaInverse = null;
                for (String invPredWiki : mapWikiPredToResult.get(predDBPedia).getInverseProperties()) {
                    if (findDBPredicateForWikiPredicate(invPredWiki, euivalentProperties) != null) {
                        dbPediaInverse = findDBPredicateForWikiPredicate(invPredWiki, euivalentProperties);
                        break;
                    }
                }
                if (dbPediaInverse != null) {
                    invPredicates.put(predDBPedia, dbPediaInverse);
                }

            }
        }
        if(CompressionEvaluator.dataset==CompressionEvaluator.Dataset.WORDNET){
            invPredicates=mapWordnetInversePredicates;
        }

        for(String pred : invPredicates.keySet()){
            String predInv = invPredicates.get(pred);
            relevantOntologyTriples.add("<"+pred+"> "+"<owl:inverseOf> <"+predInv+"> .");
        }

        return materializeInverse(invPredicates, model, addEdges);
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

    private static int getCountAndExecuteInverseUpdate(String pred, String predInv, Model model, boolean addEdges){
        String sparql;
        if(addEdges) {
            sparql = "SELECT ?s ?p ?o {\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + predInv + "> ?s }\n" +
                    "}";
        }else{
            sparql = "SELECT ?s ?p ?o WHERE{\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    FILTER (EXISTS { ?o <" + predInv + "> ?s })\n" +
                    "}";
        }

        int count = SparqlExecutor.getCount(model, sparql);

        if(addEdges) {
            sparql = "insert {?o <" + predInv + "> ?s}\n" +
                    "    WHERE { ?s <" + pred + "> ?o\n" +
                    "    MINUS { ?o <" + predInv + "> ?s }\n" +
                    "}";
        }else{
            sparql = "DELETE {?s <" + pred + "> ?o} WHERE{\n" +
                    "    ?s <" + pred + "> ?o\n" +
                    "    FILTER (EXISTS { ?o <" + predInv + "> ?s })\n" +
                    "}";
        }

        SparqlExecutor.executeSparql(model, sparql, false);
        return count;
    }

    public static int materializeInverse(Map<String, String> invPredicates, Model model, boolean addEdges) {
        int count = 0;
        for (String pred : invPredicates.keySet()) {
            String predInv = invPredicates.get(pred);
            count+=getCountAndExecuteInverseUpdate(pred,predInv,model,addEdges);
            count+=getCountAndExecuteInverseUpdate(predInv,pred,model,addEdges);
        }
        return count;
    }

    public static int materializeSymmetry(List<String> symmetricPredicates, Model model, boolean addEdge) {
        int count = 0;
        for (String pred : symmetricPredicates) {

            String sparql;

            if(addEdge) {
                sparql = "SELECT ?s ?p ?o {\n" +
                        "    ?s <" + pred + "> ?o\n" +
                        "    MINUS { ?o <" + pred + "> ?s }\n" +
                        "}";
            }else{
                sparql = "SELECT ?s ?p ?o where {\n" +
                        "    ?s <" + pred + "> ?o .\n" +
                        "    FILTER (EXISTS {?o <" + pred + "> ?s } && (str(?s) > str(?o) ))  \n" +
                        "}";
            }

            count += SparqlExecutor.getCount(model, sparql);

            if(addEdge) {
                sparql = "insert {?o <" + pred + "> ?s}\n" +
                        "    WHERE { ?s <" + pred + "> ?o\n" +
                        "    MINUS { ?o <" + pred + "> ?s }\n" +
                        "}";
            }else{
                sparql = "delete {?o <" + pred + "> ?s} where{\n" +
                        "    ?s <" + pred + "> ?o .\n" +
                        "    FILTER (EXISTS {?o <" + pred + "> ?s } && (str(?s) > str(?o) )) \n" +
                        "}";
            }

            SparqlExecutor.executeSparql(model, sparql, false);
        }
        return count;
    }


    public static int dematerializeAllTransitivePredicates(Model model, boolean addEdge, final List<String> relevantOntologyTriples) {

        List<String> transPredicates = new ArrayList<>();

        if(CompressionEvaluator.dataset == CompressionEvaluator.Dataset.DB_PEDIA){
        for (String pred : mapWikiPredToResult.keySet()) {
            if (mapWikiPredToResult.get(pred) != null && mapWikiPredToResult.get(pred).isTransitive()) {
                transPredicates.add(pred);
            }
        }
        }
        else if(CompressionEvaluator.dataset == CompressionEvaluator.Dataset.WORDNET){
            transPredicates = lstWordnetTransitivePredicates;
        }

        for(String pred : transPredicates){
            relevantOntologyTriples.add("<"+pred+"> <rdf:type> <owl:TransitiveProperty> .");
        }

        return dematerializeTransitive(transPredicates, model, addEdge);
    }


    public static int dematerializeTransitive(List<String> transPredicates, Model model, boolean addEdge) {
        int count = 0;
        for (String pred : transPredicates) {

            String sparql;
            if(!addEdge) {
                sparql = "select ?s ?p ?o\n" +
                        "WHERE { \n" +
                        "?s <" + pred + ">/<" + pred + ">+ ?o.\n" +
                        "?s <" + pred + "> ?o }";
            }else{

                sparql = "select ?s ?p ?o\n" +
                        "WHERE { \n" +
                        "?s <" + pred + ">/<" + pred + ">+ ?o .\n" +
                        "FILTER (NOT EXISTS {?s <" + pred + "> ?o })\n" +
                        "}";
            }

            count += SparqlExecutor.getCount(model, sparql);

            if(!addEdge) {
                sparql = "delete { ?s <" + pred + "> ?o }\n" +
                        "WHERE { \n" +
                        "?s <" + pred + ">/<" + pred + ">+ ?o.\n" +
                        "?s <" + pred + "> ?o }";
            }else{
                sparql = "insert { ?s <" + pred + "> ?o }\n" +
                        "WHERE { \n" +
                        "?s <" + pred + ">/<" + pred + ">+ ?o .\n" +
                        "FILTER (NOT EXISTS {?s <" + pred + "> ?o })\n" +
                        "}";
            }

            SparqlExecutor.executeSparql(model, sparql, false);

        }
        return count;
    }


    public static List<String> getAllTransitivePredicates(String ontology){
        Model model = Util.getModelFromFile(ontology);
        ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
        Set<String> transitivePedicates = new HashSet<>();
        while(tripleExtendedIterator.hasNext()){
            org.apache.jena.graph.Triple triple = tripleExtendedIterator.next();
            if(triple.getObject().isURI()){
                if(triple.getObject().getURI().toLowerCase().contains("transitiveproperty")){
                    transitivePedicates.add(triple.getSubject().getURI().toString());
                }
            }
        }
        return new ArrayList<>(transitivePedicates);
    }

    public static void main(String[] args) {

        getAllTransitivePredicates("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/ontology.rdf");

        Model m = Util.getModelFromFile("testFileSymm.ttl");
        Model ontology = Util.getModelFromFile("dbpedia_2015-04.owl");

        getWikiResults("dbpedia_2016-10.owl");

//        List<String> p = new ArrayList<>();
//        p.add("http://5");
//        materializeAllSymmetricDBPediaPredicates(m,ontology);
//        dematerializeAllTransitivePredicates(m);
//        dematerializeAllTransitivePredicates(m,p);

        Map<String,String> invPred = new HashMap<>();
        invPred.put("http://n1","http://i1");


//
//        Util.Util.printModel(m);
//        System.out.println(dematerializeTransitive(tran, m));
//        Util.Util.printModel(m);


//        materializeAllInverseDBPediaPredicates(m,ontology);


//
//        Map<String, List<String>> euivalenceMapping = new HashMap<>();
//        String key = "http://5";
//        List<String> value = new ArrayList<>();
//        value.add("http://6");
//        euivalenceMapping.put(key, value);
//
//        replaceAllEquivalentPredicates(m, euivalenceMapping);

        Model modelFromFile = Util.getModelFromFile("wordnetTransitives.ttl");
//        dematerializeAllTransitivePredicates(modelFromFile,false);
        Util.printModel(m);

//        dematerializeTransitive(tran,m,true);

//        materializeSymmetry(tran,m,false);
//        materializeInverse(invPred,m,false);


        Util.printModel(m);
    }


}
