package Util;

import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.IOException;

public class RDFTurtleConverter {

    private static final String LABEL_BLANK_NODE = "blank";


    public static ExtendedIterator<Triple> readTriplesFromRDFFile(String filePath){
        Model modelDefault = ModelFactory.createDefaultModel();
        Model modelConcrete = null;
        try {
            modelConcrete = modelDefault.read(filePath);
        } catch (Exception e) {
            return null;
        }
        return modelConcrete.getGraph().find();
    }

    public static File convertAndStoreAsTurtleFile(String filePath) throws IOException {

        ExtendedIterator<Triple> tripleExtendedIterator = readTriplesFromRDFFile(filePath);
        if (tripleExtendedIterator == null) {
            return null;
        }
        StringBuilder lines = new StringBuilder();
        while (tripleExtendedIterator.hasNext()) {
            Triple triple = tripleExtendedIterator.next();
            lines.append("<"+getLabel(triple.getSubject()) + "> <" + getLabel(triple.getPredicate()) + "> <" + getLabel(triple.getObject()) + "> .\n");
        }


        File ttlFile = new File(filePath + "_");
        if (ttlFile.exists()) {
            ttlFile.delete();
        }
        Files.write(lines.toString().getBytes(), ttlFile);

        return ttlFile;
    }


    public static String getLabel(org.apache.jena.graph.Node node){
        String label;
        if (node instanceof Node_Blank) {
            label = LABEL_BLANK_NODE;
        } else if (node instanceof Node_Literal) {
            label = node.getLiteral().toString();
        } else {
            label = node.getURI();
        }
        return label;

    }

    public static void main (String[] args) throws IOException {
        convertAndStoreAsTurtleFile("/Users/philipfrerk/Documents/RDF_data/Semantic_web_dog_food/eswc-2006-complete.rdf");
    }
}
