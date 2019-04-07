package DictionaryOptimization;

import Util.Util;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.HDTStarter;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class BlankNodesOptimizer {


    private static void optimizeBlankNodeIDs(String filePath, String newFilePath) {
        int idCounter = 0;
        Map<String, String> longToShortId = new HashMap<>();
        Model modelFromFile = Util.getModelFromFile(filePath);

        List<Triple> newTriples = new ArrayList<>();
        ExtendedIterator<Triple> tripleExtendedIterator = modelFromFile.getGraph().find();
        while (tripleExtendedIterator.hasNext()) {
            Triple triple = tripleExtendedIterator.next();

            Node subject = triple.getSubject();
            Node object = triple.getObject();
            if (triple.getSubject().isBlank()) {
                subject = constructBlankNodeWithShortId((Node_Blank) triple.getSubject(), longToShortId, idCounter++);
            }
            if (triple.getObject().isBlank()) {
                object = constructBlankNodeWithShortId((Node_Blank) triple.getObject(), longToShortId, idCounter++);
            }

            newTriples.add(Triple.create(subject, triple.getPredicate(), object));
        }

        Graph newGraph = GraphFactory.createDefaultGraph();
        for (Triple triple : newTriples) {
            newGraph.add(triple);
        }

        Model newModel = ModelFactory.createModelForGraph(newGraph);
        try {
            FileOutputStream out = new FileOutputStream(new File(newFilePath));
            newModel.write(out, "N-TRIPLE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Node_Blank constructBlankNodeWithShortId(Node_Blank nodeBlank, Map<String, String> longToShortId, int idCounter) {
        String longId = nodeBlank.getBlankNodeId().toString();
        String shortId;
        if (longToShortId.containsKey(longId)) {
            shortId = longToShortId.get(longId);
        } else {
            shortId = String.valueOf(idCounter);
            longToShortId.put(longId, shortId);
        }

        return (Node_Blank) NodeFactory.createBlankNode(shortId);
    }

    private static LinkedHashMap<CompressionResult, CompressionResult> evaluateBlankNodeOptimization(String[] filePaths) {
        LinkedHashMap<CompressionResult, CompressionResult> resultMap = new LinkedHashMap<>();

        CompressionStarter graphRePairStarter = new HDTStarter();
        for (String filePath : filePaths) {
            String newFilePath = filePath + ".opt.nt";
            CompressionResult resultOriginal = graphRePairStarter.compress(filePath, newFilePath + ".hdt", true);

            optimizeBlankNodeIDs(filePath, newFilePath);
            CompressionResult resultOptimized = graphRePairStarter.compress(newFilePath, newFilePath + ".hdt", true);

            resultMap.put(resultOriginal, resultOptimized);
        }


        return resultMap;
    }


    private static List<Triple> getTriples(Model model) {
        List<Triple> triples = new ArrayList<>();
        ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find();
        while (tripleExtendedIterator.hasNext()) {
            triples.add(tripleExtendedIterator.next());
        }
        return triples;
    }

    public static void main(String[] args) {
//        List<File> rdfFiles = Util.getAllFileRecursively(
//                "/Users/philipfrerk/Documents/RDF_data/geom", new String[]{".rdf",".ttl","nt"});
//
//        Map<File, Integer> fileToNumBlankNodes = new HashMap<>();
//        for(File file : rdfFiles){
//            fileToNumBlankNodes.put(file, checkForBlankNodes(file.getAbsolutePath()));
//        }
//
//        for(File file : fileToNumBlankNodes.keySet()){
//            System.out.println(file.getName() + " : " + fileToNumBlankNodes.get(file));
//        }

        LinkedHashMap<CompressionResult, CompressionResult> resultMap =
                evaluateBlankNodeOptimization(new String[]{"nuts-rdf-0.9.nt"});

        for (CompressionResult resultOrig : resultMap.keySet()) {
            System.out.print(resultOrig.getCompressionRatio() + ",");
        }
        System.out.println();
        for (CompressionResult resultOpt : resultMap.values()) {
            System.out.print(resultOpt.getCompressionRatio() + ",");
        }

    }
}
