package Ontology;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class OntologyEvaluator {

    public static final String FUNCTIONAL_PROPERTY = "http://www.w3.org/2002/07/owl#FunctionalProperty";
    public static final String SYMMETRIC_PROPERTY = "???";
    public static final String TRANSITIVE_PROPERTY = "???";

    public static final String EUIVALENT_PROPERTIES = "http://www.w3.org/2002/07/owl#equivalentProperty";



    public static List<String> getAllSymmetricPredicates(Model ontology) {
        return QueryExecutor.getAllPredicatesWithUnaryProperty(ontology,SYMMETRIC_PROPERTY);
    }

    public static List<String> getAllTransitivePredicates(Model ontology) {
        return QueryExecutor.getAllPredicatesWithUnaryProperty(ontology,TRANSITIVE_PROPERTY);
    }

    public static LinkedHashMap<String, List<String>> getAllEuivalentProperties(Model ontology){
        return QueryExecutor.getAllPredicateEuivClassesWithBinaryProperty(ontology, EUIVALENT_PROPERTIES);
    }

    public static void main(String[] args){
        System.out.println(getAllEuivalentProperties(Util.Util.getModelFromFile("dbpedia_2015-04.owl")).size());
    }


    }
