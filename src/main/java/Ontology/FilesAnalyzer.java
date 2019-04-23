package Ontology;

import Util.Util;

import java.io.*;
import java.util.HashMap;
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
        Map<File, Integer> fileCount = new HashMap<>();

        String dir = "/Users/philipfrerk/Downloads/";

        for(File file : new File(dir).listFiles()) {

            if(file.getName().endsWith(".ttl") || file.getName().endsWith(".nt")) {
                FileReader fi = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fi);

                String line = bufferedReader.readLine();
                int i = 0;
                while (line != null && i< Util.TRIPLE_AMOUNT) {
                    i++;
                    for (String key : inv.keySet()) {
                        String value = inv.get(key);

                        int count = 0;
                        if (fileCount.containsKey(file)) {
                            count = fileCount.get(file);
                        }

                        if (line.contains(key)) {
                            count++;
                        }
                        if (line.contains(value)) {
                            count++;
                        }
                        fileCount.put(file, count);
                    }


                    line = bufferedReader.readLine();
                }

                System.out.println(i);
            }
        }

        for(File key :fileCount.keySet()){
            System.out.println(key.getName() + " : " +fileCount.get(key));
        }
    }
}
