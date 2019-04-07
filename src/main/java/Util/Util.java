package Util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    private static final int TRIPLE_COMPONENT_LENGTH = 10;

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
        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static void writeTriplesToFile(List<Triple> triples, String filePath, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (Triple triple : triples) {
            sb.append("<" + prefix + triple.getSubject() + "> <" + prefix + triple.getPredicate() + "> <"
                    + prefix + triple.getObject() + "> .\n");
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

    public static void writeJenaTriplesToFile(List<org.apache.jena.graph.Triple> triples, String filePath, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (org.apache.jena.graph.Triple triple : triples) {
            sb.append("<" + prefix + triple.getSubject() + "> <" + prefix + triple.getPredicate() + "> <"
                    + prefix + triple.getObject() + "> .\n");
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

}
