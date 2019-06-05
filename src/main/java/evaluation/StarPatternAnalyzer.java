package evaluation;

import evaluation.RDFTurtleConverter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.*;

public class StarPatternAnalyzer {

    public static void analyze(String filePath) {
        ExtendedIterator<Triple> tripleExtendedIterator = RDFTurtleConverter.readTriplesFromRDFFile(filePath);

        int numTriples = 0;
        Set<String> setPredicateLabels = new HashSet<>();
        Set<String> setNodeLabels = new HashSet<>();

        while (tripleExtendedIterator.hasNext()) {
            numTriples++;
            Triple triple = tripleExtendedIterator.next();
            setPredicateLabels.add(RDFTurtleConverter.getLabel(triple.getPredicate()));
            setNodeLabels.add(RDFTurtleConverter.getLabel(triple.getSubject()));
            setNodeLabels.add(RDFTurtleConverter.getLabel(triple.getObject()));

        }

        System.out.println("triples: " + numTriples);
        System.out.println("predicate labels: " + setPredicateLabels.size());
        System.out.println("node labels: " + setNodeLabels.size());
        System.out.println(setPredicateLabels.size() * 1.0 / numTriples);
    }

    public static double analyzeStarSimilarity(String filePath) {
        ExtendedIterator<Triple> tripleExtendedIterator = RDFTurtleConverter.readTriplesFromRDFFile(filePath);

        List<Triple> triples = new ArrayList<>();
        while (tripleExtendedIterator.hasNext()) {
            Triple triple = tripleExtendedIterator.next();
            triples.add(triple);
        }

        SortedMap<NodeWrapper, Integer> nodeDegrees = new TreeMap<>();

        for (Triple triple : triples) {

            NodeWrapper subj = new NodeWrapper(triple.getSubject());
            NodeWrapper obj = new NodeWrapper(triple.getObject());

            if (subj.node.isURI() && !nodeDegrees.containsKey(subj)) {
                nodeDegrees.put(subj, 0);
            }
            if (obj.node.isURI() && !nodeDegrees.containsKey(obj)) {
                nodeDegrees.put(obj, 0);
            }
            if (subj.node.isURI()) {
                nodeDegrees.put(subj, nodeDegrees.get(subj) + 1);
            }
            if (obj.node.isURI()) {
                nodeDegrees.put(obj, nodeDegrees.get(obj) + 1);
            }
        }

        SortedSet<Map.Entry<NodeWrapper, Integer>> entriesSortedByValues = entriesSortedByValues(nodeDegrees);

        // top 1%
        int indexTop = (int) Math.floor(entriesSortedByValues.size() * 0.001);
        int sumDegree=0,sumDegreeTop=0;

        Iterator<Map.Entry<NodeWrapper, Integer>> iterator = entriesSortedByValues.iterator();
        int i=0;
        while (iterator.hasNext()){
            int currentDegree = iterator.next().getValue();
            if(i<indexTop){
                sumDegreeTop+=currentDegree;
            }
            sumDegree+=currentDegree;
            i++;
        }

        return 1.0*sumDegreeTop/sumDegree;
    }


    private static <K, V extends Comparable<? super V>>
    SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public static void main(String[] args) {
        System.out.println(analyzeStarSimilarity("eswc-2006-complete-alignments.rdf"));

    }

    public static class NodeWrapper implements Comparable {

        Node node;

        public NodeWrapper(Node node) {
            this.node = node;
        }

        @Override
        public int compareTo(Object o) {
            NodeWrapper nodeWrapper = (NodeWrapper) o;
            int i1 = node.getURI().hashCode();
            int i2 = nodeWrapper.node.getURI().hashCode();
            if (i1 < i2) {
                return -1;
            }
            if (i1 > i2) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return node.getURI();
        }
    }


}
