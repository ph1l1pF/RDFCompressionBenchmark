package GraphGeneration;

import Util.Triple;

import java.util.*;

public class StarPatternGenerator {


    private static List<Triple> generateStarGraphWithFixedSize(int numEdges, int numNodes, int numSubjects) {
        int numObjects = numNodes - numSubjects;

        List<Integer> availableSubj = new ArrayList<>();

        int count = 0;
        while (availableSubj.size() < numSubjects) {
            availableSubj.add(count);
            count++;
            if (count == numSubjects) {
                count = 0;
            }
        }

        count = 0;
        List<Integer> availableObj = new ArrayList<>();
        while (availableObj.size() < numObjects) {
            availableObj.add(count + numSubjects);
            count++;
            if (count == numObjects) {
                count = 0;
            }
        }

        List<Triple> triples = new ArrayList<>();
        Random random = new Random();
        while (!availableSubj.isEmpty() && !availableObj.isEmpty()) {
            int subj = availableSubj.get(random.nextInt(availableSubj.size()));
            int obj = availableObj.get(random.nextInt(availableObj.size()));

            Triple triple = new Triple(Util.Util.fillWithLeadingZeros(String.valueOf(subj)), "-", Util.Util.fillWithLeadingZeros(String.valueOf(obj)));

            if (!triples.contains(triple)) {
                triples.add(triple);
                Util.Util.removeIntFromList(availableSubj, subj);
                Util.Util.removeIntFromList(availableObj, obj);
            }

        }

        while (triples.size() < numEdges) {
            int subj = random.nextInt(numSubjects);
            int obj = Util.Util.getRandomNumberInRange(numSubjects, numNodes - 1);
            Triple triple = new Triple(Util.Util.fillWithLeadingZeros(String.valueOf(subj)), "-", Util.Util.fillWithLeadingZeros(String.valueOf(obj)));
            if (!triples.contains(triple)) {
                triples.add(triple);
            }

        }


        return triples;

    }

    /**
     *
     */
    public static List<List<Triple>> generateMultipleStarPatternGraphsWithFixedSize() {
        int steps = 40;
        int numNodes = 30 * steps;

        // compute numEdges, has to be the max-size, that one of the sets (subjects, objects) can reach
        int numEdges = numNodes - 1;

        List<List<Triple>> graphs = new ArrayList<>();

        for (int numSubjects = 1; numSubjects <= numNodes; numSubjects += steps) {
            graphs.add(generateStarGraphWithFixedSize(numEdges, numNodes, numSubjects));
        }
        return graphs;
    }
}
