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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CompressionEvaluator {

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

        evaluateCompression(filesManipulated, fileResult,true);
        System.out.println("Finished euivalence compr");
    }

    private static void evaluateInverseMaterialization(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllInverseDBPediaPredicates(model,Util.getModelFromFile(ontology));

            Util.appendStringToFile(fileResult, "# inverse replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".inv.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult,true);
        System.out.println("Finished inverse compr");
    }

    private static void evaluateSymmetricMaterialization(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
//        List<String> allSymmetricPredicates = OntologyEvaluator.getAllSymmetricPredicates(Util.getModelFromFile(ontology));
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.materializeAllSymmetricDBPediaPredicates(model,Util.getModelFromFile(ontology));

            Util.appendStringToFile(fileResult, "# symm replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".sym.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult,true);
        System.out.println("Finished symmetrical compr");
    }

    private static void evaluateTransitiveDeMaterialization(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            int numReplacements = DataReplacer.dematerializeAllTransitivePredicates(model);

            Util.appendStringToFile(fileResult, "# trans replacements in file " + fileOriginal + " : " + numReplacements);

            File fileManipulated = new File(fileOriginal + ".tra.ttl");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateCompression(filesManipulated, fileResult,true);
        System.out.println("Finished transitive compr");
    }

    private static void evaluateCompression(List<String> files, File fileResult, boolean deleteFiles) {
        final boolean addDictSize = false;

        CompressionStarter cs = new GraphRePairStarter();
        for (String file : files) {
            CompressionResult result;
            try {
                String[] split = file.split("/");
                result = cs.compress(split[split.length-1], null, addDictSize);
            } catch (Exception | OutOfMemoryError e) {
                Util.appendStringToFile(fileResult, "\n Error with file " + file + " : " + e.toString() + "\n");
                continue;
            }
            Util.appendStringToFile(fileResult, result.toString());

            if(deleteFiles){
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

        for (File file : resultFiles) {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        }

        return resultFiles;
    }

    private static List<String> prepareDataFiles() {

        // TODO: here add the files to evaluate
//        String[] originalFiles = new String[]{"instance-types_en.ttl","persondata_en.ttl","external-links_en.ttl"};
        List<String> originalFilesList = new ArrayList<>();
        File currentDir = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());

        for(File file : currentDir.listFiles()){
            if(file.getName().contains("small")){
                file.delete();
            }
        }
        for(File file : currentDir.listFiles()){
            if(file.getName().endsWith(".ttl") && !file.getName().startsWith("output")&& !file.getName().contains("small")){
                originalFilesList.add(file.getAbsolutePath());
            }
        }

        List<String> smallFiles = new ArrayList<>();
        for (String originalFile : originalFilesList) {
            Model model;
            try {
                 model = Util.getModelFromFile(originalFile, Util.TRIPLE_AMOUNT);
            }catch (Exception e){
                System.out.println("Expection in file "+originalFile);
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

        DataReplacer.getWikiResults(ontology);


        evaluateCompression(dataFiles, resultFiles.get(0),false);
        evaluateEuivReplacement(dataFiles, ontology, resultFiles.get(1));
        evaluateSymmetricMaterialization(dataFiles, ontology, resultFiles.get(2));
        evaluateTransitiveDeMaterialization(dataFiles, ontology, resultFiles.get(3));
        evaluateInverseMaterialization(dataFiles, ontology, resultFiles.get(4));



        // clean up
        for(String dataFile : dataFiles){
            new File(dataFile).delete();
        }

        time = System.currentTimeMillis()-time;
        time = time /1000.0/60/60;
        Util.sendMail(time);
    }
}
