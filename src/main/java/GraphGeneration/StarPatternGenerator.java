package GraphGeneration;

import Util.Triple;

import java.util.*;

public class StarPatternGenerator {

    /**
     * @param numEdges                 number of edges (labels are irrelevant)
     * @param numParticipatingSubjects Edges verteilen sich gleichmäßig auf so viele Subjekte
     * @param numParticipatingObjects  Edges verteilen sich gleichmäßig auf so viele Objekte
     */
    private static List<Triple> generateStarGraph(int numEdges, int numParticipatingSubjects, int numParticipatingObjects) {
        int edgesPerSubject = numEdges / numParticipatingSubjects;
        int edgesPerObject = numEdges / numParticipatingObjects;

        List<Triple> triples = new ArrayList<>();

        Map<Integer, Integer> mapObjectToCount = new HashMap<>();
        for (int i = 0; i < numParticipatingObjects; i++) {
            mapObjectToCount.put(i, 0);
        }

        for (int currentSubj = 0; currentSubj < numParticipatingSubjects; currentSubj++) {
            for (int countForCurrSubj = 0; countForCurrSubj < edgesPerSubject; countForCurrSubj++) {
                for (int currentObject = 0; currentObject < numParticipatingObjects; currentObject++) {
                    if (mapObjectToCount.get(currentObject) < edgesPerObject) {
                        Triple triple = new Triple(String.valueOf(currentSubj), "-", String.valueOf(currentObject));
                        if (!triples.contains(triple)) {
                            triples.add(triple);
                            mapObjectToCount.put(currentObject, mapObjectToCount.get(currentObject) + 1);
                        }
                    }
                }
            }
        }
        return triples;
    }

    /**
     * Each subject is connected to each object, so the number of edges will change!
     * This seems to have an impact on compr. ratio!
     * @param numNodes
     * @param numSubjects
     * @return
     */
    private static List<Triple> generateStarGraphWithFixedNumberOfNodes(int numNodes, int numSubjects) {
        // the number of objects follows
        int numObjects = numNodes - numSubjects;
        int numEdges = Math.max(numObjects, numObjects);
        List<Triple> triples = new ArrayList<>();

        // every subject is connected to every object
        for (int i = 0; i < numSubjects; i++) {
            for (int k = 0; k < numObjects; k++) {
                Triple triple = new Triple(String.valueOf(i), "-", String.valueOf(k + numSubjects));
                triples.add(triple);
            }
        }
        return triples;
    }

    public static List<List<Triple>> generateMultipleStarPatternGraphsWithFixedNoOfNodes() {
        int steps = 4;
        int numNodes = 20 * steps;


        List<List<Triple>> graphs = new ArrayList<>();

        for (int numSubjects = 1; numSubjects <= numNodes; numSubjects += steps) {
            graphs.add(generateStarGraphWithFixedNumberOfNodes(numNodes, numSubjects));
        }
        return graphs;
    }

    public static List<List<Triple>> generateMultipleStarPatternGraphs() {
        int numNodes = (int) Math.pow(2, 12); // should be 2^k for some k

        int numEdges = numNodes / 2; // must be >= numNodes/2

        List<List<Triple>> graphs = new ArrayList<>();

        int j = numNodes / 2;
        for (int i = 1; i <= numNodes / 2; i *= 2) {
            graphs.add(generateStarGraph(numEdges, i, j));
            j /= 2;
        }
        return graphs;
    }

}
