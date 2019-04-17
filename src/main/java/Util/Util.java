package Util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    private static final String HTTP_PREFIX_SUBJECT = "http://subject/";
    private static final String HTTP_PREFIX_PREDICATE = "http://predicate/";
    private static final String HTTP_PREFIX_OBJECT = "http://object/";

    private static final int TRIPLE_COMPONENT_LENGTH = 10;

    public static final int TRIPLE_AMOUNT = 200000;

    private static Random r = new Random();

    public static int roundToNearestInteger(double dec) {
        int floor = (int) Math.floor(dec);

        double distanceDown = dec - floor;
        double distanceUp = floor + 1 - dec;

        if (distanceDown < distanceUp) {
            return floor;
        } else {
            return floor + 1;
        }
    }

    public static String fillWithLeadingZeros(String tripleComponent) {
        StringBuilder leadingZeros = new StringBuilder();
        for (int i = 0; i < TRIPLE_COMPONENT_LENGTH - tripleComponent.length(); i++) {
            leadingZeros.append("0");
        }
        return leadingZeros + tripleComponent;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return r.nextInt((max - min) + 1) + min;
    }

    public static void removeIntFromList(List<Integer> list, int intToRemove) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == intToRemove) {
                list.remove(i);
                break;
            }
        }
    }

    public static Model getModelFromFile(String filePath) {
        return getModelFromFile(filePath, 1.0);
    }

    public static Model getModelFromFile(String filePath, int numTriples) {
        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }



//        StreamRDFBase destination = new Stream();
//        RDFDataMgr.parse(destination, filePath) ;


        Graph g = GraphFactory.createDefaultGraph();
        int count = 0;
        ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
        while (count < numTriples && tripleExtendedIterator.hasNext()) {
            g.add(tripleExtendedIterator.next());
            count++;
        }
        return ModelFactory.createModelForGraph(g);

    }

    public static Model getModelFromFile(String filePath, double percentage) {

        if(percentage<=0 || percentage>1){
            throw new IllegalArgumentException();
        }

        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (percentage == 1) {
            return model;
        } else {
            Graph g = GraphFactory.createDefaultGraph();
            int count = 0;
            int size = model.getGraph().size();
            ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
            while (1.0 * count / size <= percentage && tripleExtendedIterator.hasNext()) {
                g.add(tripleExtendedIterator.next());
                count++;
            }
            return ModelFactory.createModelForGraph(g);
        }
    }

    public static void printModel(Model model){
        ExtendedIterator<org.apache.jena.graph.Triple> tripleExtendedIterator = model.getGraph().find();
        System.out.println("Model:");
        while (tripleExtendedIterator.hasNext()){
            System.out.println(tripleExtendedIterator.next());
        }
        System.out.println();

    }

    public static void writeModelToFile(File file, Model model) {
        if (file.exists()) {
            file.delete();
        }
        try {
            model.write(new FileOutputStream(file), "N-TRIPLE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeTriplesToFile(List<Triple> triples, String filePath) {
        StringBuilder sb = new StringBuilder();
        for (Triple triple : triples) {
            sb.append("<" + HTTP_PREFIX_SUBJECT + triple.getSubject() + "> <" + HTTP_PREFIX_PREDICATE + triple.getPredicate() + "> <"
                    + HTTP_PREFIX_OBJECT + triple.getObject() + "> .\n");
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            Files.write(sb.toString().getBytes(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<File> getAllFileRecursively(String dirPath, String[] allowedSuffices) {
        File dir = new File(dirPath);
        List<File> files = new ArrayList<>();

        List<File> queueDirectories = new ArrayList<>();
        queueDirectories.add(dir);


        while (!queueDirectories.isEmpty()) {
            File currentDir = queueDirectories.remove(0);
            try {
                currentDir.listFiles();
            } catch (NullPointerException e) {
                continue;
            }

            for (File file : currentDir.listFiles()) {
                if (isSuffixAllowed(file.getAbsolutePath(), allowedSuffices)) {
                    try {
                        files.add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    queueDirectories.add(file);
                }
            }
        }

        return files;
    }

    private static boolean isSuffixAllowed(String file, String[] allowedSuffices) {
        for (String allowedSuffix : allowedSuffices) {
            if (file.endsWith(allowedSuffix)) {
                return true;
            }
        }
        return false;
    }

    public static String appendStringToFileName(String fileName, String string){
        String[] split = fileName.split("\\.");
        String newName = "";
        for (int i = 0; i < split.length-1; i++) {
            newName+=split[i];
            if(i<split.length-2){
                newName+=".";
            }
        }
        newName+=string+".";
        newName+=split[split.length-1];
        return newName;
    }

}
