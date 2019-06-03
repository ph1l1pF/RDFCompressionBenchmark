import evaluation.RDFTurtleConverter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.*;

public class RDFAnalyzer {

    public static void analyze(String filePath){
        ExtendedIterator<Triple> tripleExtendedIterator = RDFTurtleConverter.readTriplesFromRDFFile(filePath);

        int numTriples = 0;
        Set<String> setPredicateLabels = new HashSet<String>();
        Set<String> setNodeLabels = new HashSet<String>();

        while (tripleExtendedIterator.hasNext()) {
            numTriples++;
            Triple triple = tripleExtendedIterator.next();
            setPredicateLabels.add(RDFTurtleConverter.getLabel(triple.getPredicate()));
            setNodeLabels.add(RDFTurtleConverter.getLabel(triple.getSubject()));
            setNodeLabels.add(RDFTurtleConverter.getLabel(triple.getObject()));

        }

        System.out.println("triples: "+numTriples);
        System.out.println("predicate labels: "+setPredicateLabels.size());
        System.out.println("node labels: "+setNodeLabels.size());
        System.out.println(setPredicateLabels.size()*1.0/numTriples);
    }

    public static void analyzeStarSimilarity(String filePath){
        ExtendedIterator<Triple> tripleExtendedIterator = RDFTurtleConverter.readTriplesFromRDFFile(filePath);

        List<Triple> triples = new ArrayList<Triple>();
        while (tripleExtendedIterator.hasNext()) {
            Triple triple = tripleExtendedIterator.next();
            triples.add(triple);
        }

        Map<Node, Integer> nodeDegrees = new HashMap<Node, Integer>();

        for(Triple triple : triples){
            if(!nodeDegrees.containsKey(triple.getSubject())){
                nodeDegrees.put(triple.getSubject(), 0);
            }
            if(!nodeDegrees.containsKey(triple.getObject())){
                nodeDegrees.put(triple.getObject(), 0);
            }
            nodeDegrees.put(triple.getSubject(), nodeDegrees.get(triple.getSubject())+1);
            nodeDegrees.put(triple.getObject(), nodeDegrees.get(triple.getObject())+1);
        }

//        ValueComparator bvc = new ValueComparator(nodeDegrees);
//        TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);

        for(Node node : nodeDegrees.keySet()){
            System.out.println(node.toString() + " => " + nodeDegrees.get(node));
            if(nodeDegrees.get(node)>1){
                System.out.println("ralfk");
            }
        }
    }

   static class ValueComparator implements Comparator<Node> {
        Map<Node, Integer> base;

        public ValueComparator(Map<Node, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(Node a, Node b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    public static void main (String[] args){
//        analyzeStarSimilarity("instance-types-en-uris_es.ttl");

    }


}
