package Inference;


import Util.Util;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class QueryExecutor {

    public static ResultSet executeSparql(Model model, String sparql, boolean isQuery) {

        ParameterizedSparqlString parameterizedSparql = new ParameterizedSparqlString(model);
        parameterizedSparql.setCommandText(sparql);
//        parameterizedSparql.setParam("objectURI", resource);
//        parameterizedSparql.setParam("labelLanguage", model.createLiteral(language.getCode(), ""));

        if(isQuery) {
            Query query = QueryFactory.create(parameterizedSparql.asQuery());
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            return qexec.execSelect();
        }else{
            UpdateAction.parseExecute( parameterizedSparql.asUpdate().toString(), model );
            return null;
        }


    }

    private static List<String> getAllPredicatesWithProperty(Model ontModel, String property) {
        String spaqrl = "select ?p1" +
                "where {" +
                "?p1" + property + "?p2" +
                "}";

        ResultSet execute = executeSparql(ontModel, spaqrl,true);
        return null;
    }

    private static LinkedHashMap<String, List<String>> getAllEquivalentPredicates(Model ontModel) {
        String sparql = "select ?p1 ?p2\n" +
                "where{\n" +
                "   ?p1 <http://www.w3.org/2002/07/owl#equivalentProperty> ?p2\n" +
                "}";

        LinkedHashMap<String, List<String>> mapEquivalences = new LinkedHashMap<>();
        ResultSet resultSet = executeSparql(ontModel, sparql,true);
        while (resultSet.hasNext()) {
            QuerySolution next = resultSet.next();
            String key = next.get("?p1").toString();
            String value = next.get("?p2").toString();

            List<String> values;
            if (!mapEquivalences.containsKey(key)) {
                values = new ArrayList<>();
            } else {
                values = mapEquivalences.get(key);
            }
            values.add(value);
            mapEquivalences.put(key, values);
        }
        return mapEquivalences;
    }

    private static void replaceAllEquivalentPredicates(Model model, LinkedHashMap<String, List<String>> allEquivalentPredicates){
        for(String keyPred : allEquivalentPredicates.keySet()){
            for(String valPred : allEquivalentPredicates.get(keyPred)){
                String sparql = "DELETE {?s ?p ?o}\n" +
                        "INSERT {?s <" + keyPred+ "> ?o }\n" +
                        "WHERE  { ?s ?p ?o . \n" +
                        "         FILTER (?p = <"+ valPred +">)"  +
                        "}\n";
                executeSparql(model, sparql,false);
            }
        }
    }

    private static void printModel(Model model){
        ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find();
        while (tripleExtendedIterator.hasNext()){
            System.out.println(tripleExtendedIterator.next());
        }
    }

    public static void main(String[] args) {
//        Model model = Util.getModelFromFile("labels_en.ttl");
//        Model ontology = Util.getModelFromFile("dbpedia_2015-04.owl");
//
//
//        LinkedHashMap<String, List<String>> allEquivalentPredicates = getAllEquivalentPredicates(ontology);
//        replaceAllEquivalentPredicates(model, allEquivalentPredicates);
//
//        Util.writeModelToFile(new File("bla1.ttl"), model);

        System.out.println("hi was geht");

//

//        Dataset dataset = DatasetFactory.create();
//        dataset.addNamedModel("data",model);
//        dataset.addNamedModel("ontology",ontology);


    }

}
