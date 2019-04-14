package Inference;


import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.GraphRePairStarter;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class QueryExecutor {

    public static final String FUNCTIONAL_PROPERTY = "http://www.w3.org/2002/07/owl#FunctionalProperty";
    private static final String EUIVALENT_PROPERTIES = "http://www.w3.org/2002/07/owl#equivalentProperty";


    public static ResultSet executeSparql(Model model, String sparql, boolean isQuery) {

        ParameterizedSparqlString parameterizedSparql = new ParameterizedSparqlString(model);
        parameterizedSparql.setCommandText(sparql);
//        parameterizedSparql.setParam("objectURI", resource);
//        parameterizedSparql.setParam("labelLanguage", model.createLiteral(language.getCode(), ""));

        if (isQuery) {
            Query query = QueryFactory.create(parameterizedSparql.asQuery());
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            return qexec.execSelect();
        } else {
            UpdateAction.parseExecute(parameterizedSparql.asUpdate().toString(), model);
            return null;
        }
    }

    private static List<String> getAllPredicatesWithProperty(Model ontModel, String property) {
        String spaqrl = "select ?p1" +
                "where {" +
                "?p1 <" + property + "> ?o" +
                "}";

        ResultSet rs = executeSparql(ontModel, spaqrl, true);
        List<String> predicates = new ArrayList<>();
        while (rs.hasNext()) {
            QuerySolution next = rs.next();
            String p = next.get("?p1").toString();
            predicates.add(p);
        }
        return predicates;
    }

    private static LinkedHashMap<String, List<String>> getAllEquivalentPredicates(Model ontModel) {
        String sparql = "select ?p1 ?p2\n" +
                "where{\n" +
                "   ?p1 <"+EUIVALENT_PROPERTIES+"> ?p2\n" +
                "}";

        LinkedHashMap<String, List<String>> mapEquivalences = new LinkedHashMap<>();
        ResultSet resultSet = executeSparql(ontModel, sparql, true);
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


    private static void printModel(Model model) {
        ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find();
        while (tripleExtendedIterator.hasNext()) {
            System.out.println(tripleExtendedIterator.next());
        }
    }

    public static void main(String[] args) throws IOException {
//        Model model = Util.getModelFromFile("geo-coordinates_en.nt");
//        Model ontology = Util.getModelFromFile("dbpedia_2015-04.owl");


//        LinkedHashMap<String, List<String>> allEquivalentPredicates = getAllEquivalentPredicates(ontology);
//        replaceAllEquivalentPredicates(model, allEquivalentPredicates);

//        Util.writeModelToFile(new File("bla1.ttl"), model);

//

//        Dataset dataset = DatasetFactory.create();
//        dataset.addNamedModel("data",model);
//        dataset.addNamedModel("ontology",ontology);
        File file = new File("latestResults.txt");
        if(file.exists()){
            file.delete();
        }

        try {

            GraphRePairStarter graphRePairStarter = new GraphRePairStarter(3);
            CompressionResult compress = graphRePairStarter.compress("instance_types_dbtax-dbo.nt", null, true);

            Files.write(Paths.get(file.getAbsolutePath()), compress.toString().getBytes());
        }catch(Exception e){
            try {
                Files.write(Paths.get(file.getAbsolutePath()), e.toString().getBytes());
            } catch (IOException e1) { }

            System.out.println("error written to file");
        }
    }

}
