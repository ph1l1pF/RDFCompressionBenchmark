package GraphGeneration;

import Util.MathUtil;
import Util.Triple;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;

import java.util.*;

public class StarPatternGenerator {


    private static List<Triple> generateStarGraphWithFixedSizeNew2(int numEdges, int numNodes, int numSubjects) {
        int numObjects = numNodes - numSubjects;

        int edgesPerSubj = numEdges / numSubjects;
        int edgesPerObj = numEdges / numObjects;

        List<Integer> availableSubj = new ArrayList<>();
        for (int i = 0; i < numSubjects; i++) {
            for (int k = 0; k < edgesPerSubj; k++) {
                availableSubj.add(i);
            }

        }

        List<Integer> availableObj = new ArrayList<>();
        for (int i = 0; i < numObjects; i++) {
            for (int k = 0; k < edgesPerObj; k++) {
                availableObj.add(i + numSubjects);
            }
        }

        List<Triple> triples = new ArrayList<>();
        Random random = new Random();
        while (!availableSubj.isEmpty() && !availableObj.isEmpty()) {
            int subj = availableSubj.get(random.nextInt(availableSubj.size()));
            int obj = availableObj.get(random.nextInt(availableObj.size()));

            Triple triple = new Triple(String.valueOf(subj), "-", String.valueOf(obj));
            if (!triples.contains(triple)) {
                triples.add(triple);
                removeIntFromList(availableSubj, subj);
                removeIntFromList(availableObj, obj);
            }

        }

        while (triples.size() < numEdges) {
            int subj = random.nextInt(numSubjects);
            int obj = getRandomNumberInRange(numSubjects, numNodes - 1);
            Triple triple = new Triple(String.valueOf(subj), "-", String.valueOf(obj));
            if (!triples.contains(triple)) {
                triples.add(triple);
            }

        }


        return triples;

    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private static void removeIntFromList(List<Integer> list, int intToRemove) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == intToRemove) {
                list.remove(i);
                break;
            }
        }
    }

    /**
     *
     */
    public static List<List<Triple>> generateMultipleStarPatternGraphsWithFixedSize() {
        int steps = 4;
        int numNodes = 50 * steps;

        // compute numEdges, has to be the max-size, that one of the sets (subjects, objects) can reach
        int numEdges = numNodes - 1;

        List<List<Triple>> graphs = new ArrayList<>();

        for (int numSubjects = 1; numSubjects <= numNodes; numSubjects += steps) {
            graphs.add(generateStarGraphWithFixedSizeNew2(numEdges, numNodes, numSubjects));
        }
        return graphs;
    }

    public static void main(String[] agr) {
        generateMultipleStarPatternGraphsWithFixedSize();
        System.out.println();
    }
}
