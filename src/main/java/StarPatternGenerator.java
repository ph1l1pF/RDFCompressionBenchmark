import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarPatternGenerator {

    /**
     * @param numEdges                 number of edges (labels are irrelevant)
     * @param numParticipatingSubjects Edges verteilen sich gleichmäßig auf so viele Subjekte
     * @param numParticipatingObjects  Edges verteilen sich gleichmäßig auf so viele Objekte
     */
    private static List<Triple> generateStarGraph(int numEdges, int numParticipatingSubjects, int numParticipatingObjects) {
        int edgesPerSubject = numEdges / numParticipatingSubjects;
        int edgesPerObject = numEdges / numParticipatingObjects;

        List<Triple> triples = new ArrayList<Triple>();

        Map<Integer, Integer> mapObjectToCount = new HashMap<Integer, Integer>();
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
     *
     */
    public static List<List<Triple>> generateMultipleStarPatternGraphs() {
        int numNodes = (int) Math.pow(2, 12); // should be 2^k for some k

        int numEdges = numNodes / 2; // must be >= numNodes

        List<List<Triple>> graphs = new ArrayList<List<Triple>>();

        int j = numNodes / 2;
        for (int i = 1; i <= numNodes / 2; i *= 2) {
            graphs.add(generateStarGraph(numEdges, i, j));
            j /= 2;
        }
        return graphs;
    }
}
