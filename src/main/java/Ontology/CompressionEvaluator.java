package Ontology;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import org.apache.jena.rdf.model.Model;

import java.io.File;
import java.io.IOException;
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
            DataReplacer.replaceAllEquivalentPredicates(model, euivalentProperties);
            File fileManipulated = new File(fileOriginal + ".eq");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateData(filesManipulated, fileResult);
    }

    private static void evaluateSymmetricMaterialization(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
        List<String> allSymmetricPredicates = OntologyEvaluator.getAllSymmetricPredicates(Util.getModelFromFile(ontology));
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            DataReplacer.materializeAllSymmetricPredicates(model, allSymmetricPredicates);
            File fileManipulated = new File(fileOriginal + ".sym");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateData(filesManipulated,fileResult);
    }
    private static void evaluateTransitiveMaterialization(List<String> files, String ontology, File fileResult) {
        List<String> filesManipulated = new ArrayList<>();
        List<String> allTransitivePredicates = OntologyEvaluator.getAllTransitivePredicates(Util.getModelFromFile(ontology));
        for (String fileOriginal : files) {
            Model model = Util.getModelFromFile(fileOriginal);
            DataReplacer.dematerializeAllTransitivePredicates(model, allTransitivePredicates);
            File fileManipulated = new File(fileOriginal + ".tra");
            Util.writeModelToFile(fileManipulated, model);
            filesManipulated.add(fileManipulated.getAbsolutePath());
        }

        evaluateData(filesManipulated,fileResult);
    }

    private static void evaluateData(List<String> files, File fileResult) {
        List<CompressionResult> results= new ArrayList<>();

        final boolean addDictSize = false;

        CompressionStarter cs = new GraphRePairStarter();
        for (String fileOriginal : files) {
            CompressionResult result;
            try {
                 result = cs.compress(fileOriginal, null, addDictSize);
            }catch(Exception | OutOfMemoryError e){
                continue;
            }
            results.add(result);
            appendResultToFile(fileResult,result);
        }

    }

    private static void appendResultToFile(File file, CompressionResult result){
        try {
            Files.write(Paths.get(file.getAbsolutePath()), result.toString().getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            // will not happen
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        File dirResults = new File("Latest_Results");
        if(!dirResults.exists()) {
            dirResults.mkdir();
        }
        List<File> resultFiles = new ArrayList<>();
        resultFiles.add(new File(dirResults.getName()+"/original_results.txt"));
        resultFiles.add(new File(dirResults.getName()+"/equivalence_results.txt"));
        resultFiles.add(new File(dirResults.getName()+"/symmetrie_results.txt"));
        resultFiles.add(new File(dirResults.getName()+"/transitive_results.txt"));

        for(File file : resultFiles){
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
        }

        // evaluate original files
        List<String> files = new ArrayList<>();
        files.add("instance-types_en.ttl");
        evaluateData(files, resultFiles.get(0));
    }
}
