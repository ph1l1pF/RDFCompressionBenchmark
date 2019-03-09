import java.util.ArrayList;
import java.util.List;

public class StarPatternGenerator {

    /**
     * @param numEdges number of edges (labels are irrelevant)
     * @param numParticipatingSubjects Edges verteilen sich gleichmäßig auf so viele Subjekte
     * @param numParticipatingObjects  Edges verteilen sich gleichmäßig auf so viele Objekte
     */
    private static List<Triple> generateStarGraph(int numEdges, int numParticipatingSubjects, int numParticipatingObjects) {
        int edgesPerSubject = numEdges / numParticipatingSubjects;
        int edgesPerObject = numEdges / numParticipatingObjects;

        List<Triple> triples = new ArrayList<Triple>();

        int currentObj = 0;
        int countForCurrentObj = 0; // must always be <= edgesPerObject

        for (int currentSubj = 0; currentSubj < numParticipatingSubjects; currentSubj++) {
            for (int countForCurrSubj = 0; countForCurrSubj < edgesPerSubject; countForCurrSubj++) {
                if (countForCurrentObj < edgesPerObject) {
                    // new edge from currentSubject to currentObject
                    triples.add(new Triple(String.valueOf(currentSubj), "-", String.valueOf(currentObj)));
                    countForCurrentObj++;
                } else {
                    // current object has already enough ingoing edges, so go to next object
                    currentObj++;
                    countForCurrentObj = 0;

                    // but we still want the same subject in next iteration
//                    countForCurrSubj--;
                }
            }
        }

        return triples;
    }

    /**
     *
     */
    public static List<List<Triple>> generateMultipleStarPatternGraphs() {
        int numNodes = (int)Math.pow(2, 18); // should be 2^k for some k

        int numEdges = numNodes; // must be >= numNodes

        List<List<Triple>> graphs = new ArrayList<List<Triple>>();

        int j = numNodes/2;
        for (int i = 1; i <= numNodes/2; i *= 2) {
            graphs.add(generateStarGraph(numEdges, i, j));
            j /= 2;
        }
        return graphs;
    }



    public static void main(String[] args) {
        generateMultipleStarPatternGraphs();
    }
}
