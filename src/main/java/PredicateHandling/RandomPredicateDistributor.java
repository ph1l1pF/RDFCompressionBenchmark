package PredicateHandling;

import Util.Triple;
import evaluation.StarGraphEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPredicateDistributor implements PredicateDistributor {
    private Random random = new Random();

    public void distributePredicates(List<Triple> triples, List<String> predicates) {

        for (Triple triple : triples) {
            String predicate = predicates.get(random.nextInt(predicates.size()));
            // all predicates should have the same length as string

            predicate = Util.Util.fillWithLeadingZeros(predicate);
            triple.setPredicate(predicate);
        }
    }
}
