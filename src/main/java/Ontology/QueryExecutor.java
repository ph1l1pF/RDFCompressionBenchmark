package Ontology;


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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class QueryExecutor {


    public static ResultSet executeSparql(Model model, String sparql, boolean isQuery) {
        ParameterizedSparqlString parameterizedSparql = new ParameterizedSparqlString(model);
        parameterizedSparql.setCommandText(sparql);

        if (isQuery) {
            Query query = QueryFactory.create(parameterizedSparql.asQuery());
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            return qexec.execSelect();
        } else {
            UpdateAction.parseExecute(parameterizedSparql.asUpdate().toString(), model);
            return null;
        }
    }


    public static List<String> getAllPredicatesWithUnaryProperty(Model ontology, String property){
        String spaqrl = "select ?p1" +
                "where {" +
                "?p1 <" + property + "> ?o" +
                "}";

        ResultSet rs = QueryExecutor.executeSparql(ontology, spaqrl, true);
        List<String> predicates = new ArrayList<>();
        while (rs.hasNext()) {
            QuerySolution next = rs.next();
            String p = next.get("?p1").toString();
            predicates.add(p);
        }
        return predicates;
    }

    public static int countAllPredicatesWithUnaryProperty(Model ontology, String property){
        return getAllPredicatesWithUnaryProperty(ontology,property).size();
    }


    public static LinkedHashMap<String, List<String>> getAllPredicateEuivClassesWithBinaryProperty(Model ontology, String property){
        String sparql = "select ?p1 ?p2\n" +
                "where{\n" +
                "   ?p1 <"+property+"> ?p2\n" +
                "}";

        LinkedHashMap<String, List<String>> mapEquivalences = new LinkedHashMap<>();
        ResultSet resultSet = QueryExecutor.executeSparql(ontology, sparql, true);
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

    public static int countAllPredicateEuivClassesWithBinaryProperty(Model ontology, String property){
        return getAllPredicateEuivClassesWithBinaryProperty(ontology,property).size();
    }

    public static long countTriplesContainingPredicate(Model data, String predicate){
        String spaqrl = "select ?s ?p1 ?o \n" +
                "where {\n" +
                "?s <" + predicate + "> ?o \n" +
                "}";

        ResultSet rs = QueryExecutor.executeSparql(data, spaqrl, true);
        long numTriples=0;
        while (rs.hasNext()) {
            rs.next();
            numTriples++;
        }
        return numTriples;
    }



    public static void main(String[] args)  {
        File file = new File("latestResults.txt");
        if(file.exists()){
            file.delete();
        }

        try {
            GraphRePairStarter graphRePairStarter = new GraphRePairStarter();

            String name = "instance_types_dbtax-dbo.nt";
            Model modelFromFile = Util.getModelFromFile(name, 0.1);
            String newName = "newFile.ttl";
            Util.writeModelToFile(new File(newName), modelFromFile);

            System.out.println("fertig");

            CompressionResult result = graphRePairStarter.compress(newName, null, true);

            Files.write(Paths.get(file.getAbsolutePath()), result.toString().getBytes());
        }catch(OutOfMemoryError e){
            try {
                Files.write(Paths.get(file.getAbsolutePath()), e.toString().getBytes());
            } catch (IOException e1) { }

            System.out.println("error written to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
