package Ontology;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WikiDataHandler implements Serializable {

    private static final String TRANSITIVE_PROPERTY = "Q18647515";
    private static final String SYMMETRIC_PROPERTY = "Q21510862";
    private static final String INVERSE_PROPERTY = "P1696";



    public static Result getResultForPredicate(String predDBPedia, String predWikiData) {
        Model ontology = ModelFactory.createDefaultModel();

        ontology.add(ontology.createResource(predDBPedia), RDF.type, RDF.Property);
        ontology.add(ontology.createResource(predDBPedia), OWL.equivalentProperty,
                ontology.createResource(predWikiData));

        ResIterator iterator = ontology.listSubjectsWithProperty(RDF.type, RDF.Property);
        Result result = new Result();
        while (iterator.hasNext()) {
            Resource dbpProp = iterator.next();
            NodeIterator obIter = ontology.listObjectsOfProperty(dbpProp, OWL.equivalentProperty);

            while (obIter.hasNext()) {
                RDFNode eqProp = obIter.next();
                if (eqProp.isURIResource() && eqProp.asResource().getURI().startsWith("http://www.wikidata.org")) {
                    Model retrievedModel = ModelFactory.createDefaultModel();
                    try {
                        retrievedModel.read(eqProp.asResource().getURI());
                    }catch (RiotNotFoundException e){
                        // resource not found in wiki data => continue
                        System.out.println("--------\n raaaalf\n");
                        return null;
                    }
                    // process info from Wikidata, e.g., inverse property
                    if (retrievedModel.contains(eqProp.asResource(), OWL.inverseOf)) {
                        NodeIterator invIter = retrievedModel.listObjectsOfProperty(eqProp.asResource(), OWL.inverseOf);
                        while (invIter.hasNext()) {
                            RDFNode next = invIter.next();
                            ontology.add(dbpProp, OWL.inverseOf, next);
                            result.getInverseProperties().add(next.toString()); //TODO passt das?
                        }
                    }

                    ExtendedIterator<Triple> tripleExtendedIterator = retrievedModel.getGraph().find();
                    while(tripleExtendedIterator.hasNext()){
                        Triple triple = tripleExtendedIterator.next();
                        if(triple.getObject().toString().contains(SYMMETRIC_PROPERTY)){
                            ontology.add(dbpProp, RDF.type, OWL.SymmetricProperty);
                            result.setSymmetric(true);
                        }else if(triple.getObject().toString().contains(TRANSITIVE_PROPERTY)){
                            ontology.add(dbpProp, RDF.type, OWL.TransitiveProperty);
                            result.setTransitive(true);
                        }
                        else if(triple.getPredicate().toString().contains(INVERSE_PROPERTY)){
                            result.getInverseProperties().add(triple.getObject().toString());
                        }
                    }

                }
            }
        }
        return result;
    }


    public static class Result implements Serializable{
        private List<String> inverseProperties = new ArrayList<>();
        private boolean isSymmetric=false;
        private boolean isTransitive=false;


        public List<String> getInverseProperties() {
            return inverseProperties;
        }

        public boolean isSymmetric() {
            return isSymmetric;
        }

        public boolean isTransitive() {
            return isTransitive;
        }

        public void setInverseProperties(List<String> inverseProperties) {
            this.inverseProperties = inverseProperties;
        }

        public void setSymmetric(boolean symmetric) {
            isSymmetric = symmetric;
        }

        public void setTransitive(boolean transitive) {
            isTransitive = transitive;
        }
    }
}
