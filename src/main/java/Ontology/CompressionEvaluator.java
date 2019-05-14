package Ontology;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;

public class CompressionEvaluator {

    private static final String TRANSITIVE = "transitive";
    private static final String SYMMETRIC = "symmetric";
    private static final String INVERSE = "inverse";
    private static final String EQUIVALENT = "equivalent";
    private static final String EVERYTHING = "everything";


    /*
    Configurations
     */

    private static final String FILE_TO_EVALUATE = "wordnet_withmanyinverse.ttl";

    private static final boolean EVALUATE_CURRENT_DICT = false;

    private static Map<String, Boolean> mapMaterialization = new HashMap<>();
    private static Map<String, Boolean> mapFeatures = new HashMap<>();

    static {
        mapMaterialization.put(SYMMETRIC, true);
        mapMaterialization.put(INVERSE, true);
        mapMaterialization.put(TRANSITIVE, false);


        mapFeatures.put(SYMMETRIC, false);
        mapFeatures.put(INVERSE, true);
        mapFeatures.put(TRANSITIVE, false);
        mapFeatures.put(EQUIVALENT, false);
        mapFeatures.put(EVERYTHING, false);
    }


    public static final Dataset dataset = Dataset.WORDNET;

    public enum Dataset{
        DB_PEDIA, WORDNET
    }

    //----------End configurations------------------------------------

    private static void evaluateEuivReplacement(List<String> files, String ontology, File fileResult) {
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

        evaluateCompression(filesManipulated, fileResult, true);
        System.out.println("Finished euivalence compr");
    }

    private static void evaluateInverseMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllInverseDBPediaPredicates(model, Util.getModelFromFile(ontology), addEdges);

            Util.appendStringToFile(fileResult, "# inverse replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".inv.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult, true);
        System.out.println("Finished inverse compr");
    }

    private static void evaluateSymmetricMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
//        List<String> allSymmetricPredicates = OntologyEvaluator.getAllSymmetricPredicates(Util.getModelFromFile(ontology));
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllSymmetricDBPediaPredicates(model, Util.getModelFromFile(ontology), addEdges);

            Util.appendStringToFile(fileResult, "# symm replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".sym.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult, true);
        System.out.println("Finished symmetrical compr");
    }

    private static void evaluateTransitiveDeMaterialization(List<String> files, String ontology, File fileResult, boolean addEdges) {
        List<String> filesManipulated = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.dematerializeAllTransitivePredicates(model, addEdges);

            Util.appendStringToFile(fileResult, "# trans replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".tra.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult, true);
        System.out.println("Finished transitive compr");
    }

    private static void evaluateEverything(List<String> files, String ontology, File fileResult, Map<String, Boolean> mapMaterialization) {
        List<String> filesManipulated = new ArrayList<>();
        Model modelOnt = Util.getModelFromFile(ontology);

        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = 0;

            numReplacements += DataReplacer.materializeAllSymmetricDBPediaPredicates(model, modelOnt, mapMaterialization.get(SYMMETRIC));
            numReplacements += DataReplacer.materializeAllInverseDBPediaPredicates(model, model, mapMaterialization.get(INVERSE));
            numReplacements += DataReplacer.dematerializeAllTransitivePredicates(model, mapMaterialization.get(TRANSITIVE));

            File fileManipulated = new File(fileOriginal + ".all.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }
        evaluateCompression(filesManipulated, fileResult, true);
        System.out.println("Finished everything compr");
    }


    private static void evaluateCompression(List<String> files, File fileResult, boolean deleteFiles) {
        // TODO: sinnvoll?
        final boolean addDictSize = true;

        CompressionStarter cs = new GraphRePairStarter(6);
        for (String file : files) {
            CompressionResult result;
            try {
                String[] split = file.split("/");
                result = cs.compress(split[split.length - 1], null, addDictSize);
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

    public static void main(String[] args) throws IOException {

        double time = System.currentTimeMillis();

        List<File> resultFiles = prepareResultFiles();
        List<String> dataFiles = prepareDataFiles();
        final String ontology = "dbpedia_2015-04.owl";


        evaluateCompression(dataFiles, resultFiles.get(0), false);

        if(mapFeatures.get(EQUIVALENT)) {
            evaluateEuivReplacement(dataFiles, ontology, resultFiles.get(1));
        }
        if(mapFeatures.get(SYMMETRIC)) {
            evaluateSymmetricMaterialization(dataFiles, ontology, resultFiles.get(2), mapMaterialization.get(SYMMETRIC));
        }
        if(mapFeatures.get(TRANSITIVE)) {
            evaluateTransitiveDeMaterialization(dataFiles, ontology, resultFiles.get(3), mapMaterialization.get(TRANSITIVE));
        }
        if(mapFeatures.get(INVERSE)) {
            evaluateInverseMaterialization(dataFiles, ontology, resultFiles.get(4), mapMaterialization.get(INVERSE));
        }
        if(mapFeatures.get(EVERYTHING)) {
            evaluateEverything(dataFiles, ontology, resultFiles.get(5), mapMaterialization);
        }

        // clean up
        for (String dataFile : dataFiles) {
            new File(dataFile).delete();
        }

//        time = System.currentTimeMillis()-time;
//        time = time /1000.0/60/60;
//        Util.sendMail(time);
    }
}
