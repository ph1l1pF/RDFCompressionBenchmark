package Ontology;

import Util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesAnalyzer {

    public static void main(String[] a) throws IOException {
        Map<String,String> inv = new HashMap<>();


        inv.put("http://dbpedia.org/ontology/doctoralStudent" , "http://dbpedia.org/ontology/doctoralAdvisor");
        inv.put("http://dbpedia.org/ontology/mother" , "http://dbpedia.org/ontology/child");
        inv.put("http://dbpedia.org/ontology/father" , "http://dbpedia.org/ontology/child");
        inv.put("http://dbpedia.org/ontology/child" , "http://dbpedia.org/ontology/mother");
        inv.put("http://dbpedia.org/ontology/follows" , "http://dbpedia.org/ontology/followedBy");
        inv.put("http://dbpedia.org/ontology/followedBy" , "http://dbpedia.org/ontology/follows");
        inv.put("http://dbpedia.org/ontology/doctoralAdvisor" , "http://dbpedia.org/ontology/doctoralStudent");
        inv.put("http://dbpedia.org/ontology/spouse" , "http://dbpedia.org/ontology/spouse");

        List<String> trans = new ArrayList<>();
//        trans.add("http://dbpedia.org/ontology/isPartOf");
//        trans.add("http://dbpedia.org/ontology/province");
//        trans.add("http://dbpedia.org/ontology/locatedInArea");
//        trans.add("http://dbpedia.org/ontology/city");
//        trans.add("http://dbpedia.org/ontology/district");
//        trans.add("http://dbpedia.org/ontology/county");
//        trans.add("http://dbpedia.org/ontology/settlement");


//        trans.add("http://dbpedia.org/ontology/spouse");
        trans.add("http://wordnet-rdf.princeton.edu/ontology#antonym");




        File file = new File("wordnet.nt");
        int numTriples=0,numOccurrences=0;

        if (file.getName().endsWith(".ttl") || file.getName().endsWith(".nt")) {
            FileReader fi = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fi);

            String line = bufferedReader.readLine();
            while (line != null) {
                numTriples++;
                for (String item : trans) {

                    if (line.contains(item)) {
                        numOccurrences++;
                    }

                }




                line = bufferedReader.readLine();
            }

            System.out.println(1.0*numOccurrences/numTriples);
        }



    }
}
