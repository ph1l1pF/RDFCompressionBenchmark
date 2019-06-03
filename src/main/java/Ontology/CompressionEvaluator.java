package Ontology;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CompressionEvaluator {

    private static final String ORIGINAL = "original";
    public static final String TRANSITIVE = "transitive";
    public static final String SYMMETRIC = "symmetric";
    public static final String INVERSE = "inverse";
    private static final String EQUIVALENT = "equivalent";
    private static final String EVERYTHING = "everything";


    /*
    Configurations
     */

    private static final String FILE_TO_EVALUATE = "mappingbased-properties_en_manyinversesBigger.ttl";

    private static final boolean EVALUATE_CURRENT_DICT = false;

    private static final Map<String, Boolean> mapMaterialization = new HashMap<>();
    private static final Map<String, Boolean> mapActiveFeatures = new HashMap<>();

    static {
        mapMaterialization.put(SYMMETRIC, false);
        mapMaterialization.put(INVERSE, true);
        mapMaterialization.put(TRANSITIVE, false);

        mapActiveFeatures.put(SYMMETRIC, false);
        mapActiveFeatures.put(INVERSE, true);
        mapActiveFeatures.put(TRANSITIVE, false);
        mapActiveFeatures.put(EQUIVALENT, false);
        mapActiveFeatures.put(EVERYTHING, false);
    }


    public static final Dataset dataset = Dataset.DB_PEDIA;

    public static enum Dataset {
        DB_PEDIA, WORDNET
    }

    //----------End configurations------------------------------------


    private static final CompressionStarter cs = new GraphRePairStarter(6);


    private static List<CompressionResult> evaluateEuivReplacement(List<String> files, String ontology, File fileResult) {
        //TODO: hier hab ich das nicht mit den ontology relevanten predicates gemacht
        List<String> triplesForOntology = new ArrayList<>();

        List<String> filesManipulated = new ArrayList<>();
        LinkedHashMap<String, List<String>> euivalentProperties = OntologyEvaluator.getAllEuivalentProperties(Util.getModelFromFile(ontology));
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.replaceAllEquivalentPredicates(model, euivalentProperties);
            Util.appendStringToFile(fileResult, "# euiv replacements in file " + fileOriginal + " : " + numReplacements);


            File fileManipulated = new File(fileOriginal + ".eq.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        return evaluateCompression(filesManipulated, fileResult, true, triplesForOntology);
    }

    private static List<CompressionResult> evaluateInverseMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
        List<String> triplesForOntology = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllInverseDBPediaPredicates(model, Util.getModelFromFile(ontology), addEdges, triplesForOntology);

            Util.appendStringToFile(fileResult, "# inverse replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".inv.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        return evaluateCompression(filesManipulated, fileResult, true, triplesForOntology);
    }


    private static List<CompressionResult> evaluateSymmetricMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
        List<String> triplesForOntology = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllSymmetricDBPediaPredicates(model, Util.getModelFromFile(ontology), addEdges, triplesForOntology);

            Util.appendStringToFile(fileResult, "# symm replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".sym.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        return evaluateCompression(filesManipulated, fileResult, true, triplesForOntology);
    }

    private static List<CompressionResult> evaluateTransitiveDeMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
        List<String> triplesForOntology = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.dematerializeAllTransitivePredicates(model, addEdges, triplesForOntology);

            Util.appendStringToFile(fileResult, "# trans replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".tra.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        return evaluateCompression(filesManipulated, fileResult, true, triplesForOntology);
    }

    private static List<CompressionResult> evaluateEverything(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
        Model modelOnt = Util.getModelFromFile(ontology);
        List<String> triplesForOntology = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = 0;

            numReplacements += DataReplacer.materializeAllSymmetricDBPediaPredicates(model, modelOnt, mapMaterialization.get(SYMMETRIC), triplesForOntology);
            numReplacements += DataReplacer.materializeAllInverseDBPediaPredicates(model, modelOnt, mapMaterialization.get(INVERSE), triplesForOntology);
            numReplacements += DataReplacer.dematerializeAllTransitivePredicates(model, mapMaterialization.get(TRANSITIVE), triplesForOntology);

            File fileManipulated = new File(fileOriginal + ".all.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }
        return evaluateCompression(filesManipulated, fileResult, true, triplesForOntology);
    }

    private static void appendRelevantOntTriplesToFile(String file, List<String> triplesForOntology) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String triple : triplesForOntology) {
            stringBuilder.append(triple);
            stringBuilder.append("\n");
        }
        try {
            Files.write(Paths.get(file), stringBuilder.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<CompressionResult> evaluateCompression(List<String> files, File fileResult, boolean deleteFiles, List<String> triplesForOntology) {
        if (triplesForOntology != null && !triplesForOntology.isEmpty()) {
            for (String file : files) {
                appendRelevantOntTriplesToFile(file, triplesForOntology);
            }
        }

        // TODO: sinnvoll?
        final boolean addDictSize = false;

        List<CompressionResult> results = new ArrayList<>();
        for (String file : files) {
            CompressionResult result;
            try {
                String[] split = file.split("/");
                result = cs.compress(split[split.length - 1], null, addDictSize);
                results.add(result);
                System.out.println("\n----------\n");
            } catch (Exception | OutOfMemoryError e) {
                Util.appendStringToFile(fileResult, "\n Error with file " + file + " : " + e.toString() + "\n");
                continue;
            }
            Util.appendStringToFile(fileResult, result.toString());

            if (deleteFiles) {
                new File(file).delete();
            }
        }


        return results;
    }


    private static List<File> prepareResultFiles() throws IOException {
        File dirResults = new File("Latest_Results");
        if (!dirResults.exists()) {
            dirResults.mkdir();
        }
        List<File> resultFiles = new ArrayList<>();
        resultFiles.add(new File(dirResults.getName() + "/original_results.txt"));
        resultFiles.add(new File(dirResults.getName() + "/equivalence_results.txt"));
        resultFiles.add(new File(dirResults.getName() + "/symmetrie_results.txt"));
        resultFiles.add(new File(dirResults.getName() + "/transitive_results.txt"));
        resultFiles.add(new File(dirResults.getName() + "/inverse_results.txt"));
        resultFiles.add(new File(dirResults.getName() + "/everything_results.txt"));

        for (File file : resultFiles) {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        }

        return resultFiles;
    }

    private static List<String> prepareDataFiles() {

        File currentDir = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());

        for (File file : currentDir.listFiles()) {
            if (file.getName().contains("small")) {
                file.delete();
            }
        }

        List<String> originalFilesList = new ArrayList<>();
        if (!EVALUATE_CURRENT_DICT) {
            String[] files = new String[]{FILE_TO_EVALUATE};
            for (String file : files) {
                originalFilesList.add(file);
            }
        } else {
            for (File file : currentDir.listFiles()) {
                if (file.getName().endsWith(".ttl") && !file.getName().startsWith("output") && !file.getName().contains("small")) {
                    originalFilesList.add(file.getAbsolutePath());
                }
            }
        }


        List<String> smallFiles = new ArrayList<>();
        for (String originalFile : originalFilesList) {
            Model model;
            try {
                model = Util.getModelFromFile(originalFile, Util.TRIPLE_AMOUNT);
            } catch (Exception e) {
                System.out.println("Exception in file " + originalFile);
                continue;
            }
            String small = Util.appendStringToFileName(originalFile, "_small");
            File smallFile = new File(small);
            Util.writeModelToFile(smallFile, model);

            smallFiles.add(smallFile.getAbsolutePath());
        }

        System.out.println("Finished data files preparation.");

        return smallFiles;
    }

    private static CompressionResult compressRelevantTriplesOfOntology(List<String> triplesForOntology) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String triple : triplesForOntology) {
            stringBuilder.append(triple);
            stringBuilder.append("\n");
        }
        final String ontology = "ontology.ttl";
        Files.write(Paths.get(ontology), stringBuilder.toString().getBytes());

        return cs.compress(ontology, null, true);
    }

    private static void printEvaluationResults(Map<String, List<CompressionResult>> mapResults) {
        long compressedSizeOrignal = mapResults.get(ORIGINAL).get(0).getCompressedSize();

        System.out.println("\n\nevalutation result\n");
        System.out.println("orginal compr size: " + compressedSizeOrignal);

        for (String feature : mapResults.keySet()) {
            if (!feature.equals(ORIGINAL)) {
                long compressedSize = mapResults.get(feature).get(0).getCompressedSize();
                System.out.println(feature + " compr size: " + compressedSize);
                double ratio = 1.0*compressedSize/compressedSizeOrignal;
                System.out.print(feature + " ratio: " + ratio);
            }
        }

    }

    public static void main(String[] args) throws IOException {

        List<File> resultFiles = prepareResultFiles();
        List<String> dataFiles = prepareDataFiles();
        final String ontology = "dbpedia_2015-04.owl";

        Map<String, List<CompressionResult>> mapResults = new LinkedHashMap<>();

        mapResults.put(ORIGINAL, evaluateCompression(dataFiles, resultFiles.get(0), false, null));

        if (mapActiveFeatures.get(EQUIVALENT)) {
            mapResults.put(EQUIVALENT, evaluateEuivReplacement(dataFiles, ontology, resultFiles.get(1)));
        }
        if (mapActiveFeatures.get(SYMMETRIC)) {
            mapResults.put(SYMMETRIC, evaluateSymmetricMaterialization(dataFiles, ontology, resultFiles.get(2), mapMaterialization.get(SYMMETRIC)));
        }
        if (mapActiveFeatures.get(TRANSITIVE)) {
            mapResults.put(TRANSITIVE, evaluateTransitiveDeMaterialization(dataFiles, ontology, resultFiles.get(3), mapMaterialization.get(TRANSITIVE)));
        }
        if (mapActiveFeatures.get(INVERSE)) {
            mapResults.put(INVERSE, evaluateInverseMaterialization(dataFiles, ontology, resultFiles.get(4), mapMaterialization.get(INVERSE)));
        }
        if (mapActiveFeatures.get(EVERYTHING)) {
            mapResults.put(EVERYTHING, evaluateEverything(dataFiles, ontology, resultFiles.get(5)));
        }

        printEvaluationResults(mapResults);

        // clean up
        for (String dataFile : dataFiles) {
            new File(dataFile).delete();
        }
    }
}
