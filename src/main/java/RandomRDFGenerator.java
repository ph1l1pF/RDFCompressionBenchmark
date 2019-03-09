
import compressionHandling.GraphRePairStarter;

import java.io.*;
import java.util.*;

public class RandomRDFGenerator {


    //    Barabasi
    public static void generateRandomRDF(int numNodeLabels, int numEdgeLabels, int numTriples) {

        List<Triple> triples = new ArrayList<Triple>(numTriples);
        Random random = new Random();
        for (int i = 0; i < numTriples; i++) {

            System.out.println("iteration " + i);

            int subject = random.nextInt(numNodeLabels);
            int object = random.nextInt(numNodeLabels);
            int predicate = random.nextInt(numEdgeLabels);

            String prefix = "https://w3id.org/scholarlydata/";

            Triple triple = new Triple(prefix + subject, prefix + predicate, prefix + object);
            if (triples.contains(triple)) {
                i--;
                continue;
            } else {
                triples.add(triple);
            }
        }

        writeTriplesToFile(triples);

    }

    private static void generateWithDigram(int numNodeLabels, int numEdgeLabels, int numTriples, int numOccurrences) {
        List<Triple> triples = new ArrayList<Triple>(numTriples);
        Random random = new Random();


        int digramFrequency = numTriples/numOccurrences * 2 ;


        int lastObject = -1;
        for (int i = 0; i < numTriples; i++) {

            int subject = 0, object=0, predicate=0;

            if (i % digramFrequency == 0 || i % digramFrequency == 1) {
                predicate = 1;
                object = random.nextInt(numNodeLabels);
                if(i % digramFrequency == 0 ) {
                    subject = random.nextInt(numNodeLabels);
                    lastObject = object;
                }else if(i % digramFrequency == 1 ) {
                    subject = lastObject;
                    object = random.nextInt(numNodeLabels);
                    lastObject=-1;
                }

            } else {
                subject = random.nextInt(numNodeLabels);
                object = random.nextInt(numNodeLabels);
                predicate = random.nextInt(numEdgeLabels);
            }

            System.out.println("iteration " + i);


            String prefix = "https://w3id.org/scholarlydata/";

            Triple triple = new Triple(prefix + subject, prefix + predicate, prefix + object);
            if (triples.contains(triple)) {
                i--;
                continue;
            } else {
                triples.add(triple);
            }
        }

        writeTriplesToFile(triples);
    }

    private static void writeTriplesToFile(List<Triple> triples) {
        try {
            File file = new File("file.rdf");
            file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (Triple triple : triples) {
                writer.write("<" + triple.subject + "> <" + triple.predicate + "> <" + triple.object + "> .");
                writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("something messed up");
            System.exit(1);
        }

    }

    private static class Triple {
        String subject, predicate, object;

        Triple(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public boolean equals(Object obj) {
            Triple triple = (Triple) obj;
            return subject.equals(triple.subject) && predicate.equals(triple.predicate) && object.equals(triple.object);
        }
    }

    public static void main(String[] r) {

//        generateWithDigram(20, 3, 1000, 200);
//        GraphRePairStarter.compress("instance-types-en-uris_es.ttl");


//        Map<Integer, Double> mapIterationToRatio = new HashMap<Integer, Double>();
//
//        for (int i = 1; i <= 10; i++) {
//            generateRandomRDF(100, i * 100, 1000);
//
//            compressionHandling.GraphRePairStarter.compress("file.rdf");
//
//            long origLength = new File("file.rdf").length();
//
//            long comprLength = 0;
//            File directory = new File("output_file.rdf");
//            for(File file : directory.listFiles()){
//                if(file.isFile()){
//                    comprLength+=file.length();
//                }
//            }
//
//            mapIterationToRatio.put(i, 1.0*comprLength/origLength);
//        }

//        compressionHandling.GraphRePairStarter.deCompress("file.rdf","file_out");
    }

}
