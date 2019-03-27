package Util;

import compressionHandling.HDTStarter;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public static File convertAndStoreAsTurtleFile(String filePath) {

        ExtendedIterator<Triple> tripleExtendedIterator = readTriplesFromRDFFile(filePath);
        if (tripleExtendedIterator == null) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 0;

        while (tripleExtendedIterator.hasNext()) {

            if (counter > 10000) {
                counter = 0;
                lines.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
            counter++;

            Triple triple = tripleExtendedIterator.next();
            stringBuilder.append("<" + getLabel(triple.getSubject()) + "> <" + getLabel(triple.getPredicate()) + "> <" + getLabel(triple.getObject()) + "> .\n");
        }

        System.out.println("done");

        File ttlFile = new File(filePath + "_");
        if (ttlFile.exists()) {
            ttlFile.delete();
        }

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(ttlFile);
            bw = new BufferedWriter(fw);
            for (String line : lines) {
                bw.write(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

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
