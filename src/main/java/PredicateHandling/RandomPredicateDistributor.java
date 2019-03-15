package PredicateHandling;

import Util.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPredicateDistributor implements PredicateDistributor {


    public void distributePredicates(List<Triple> triples, List<String> predicates) {
        Random random = new Random();
        for (Triple triple : triples) {
            String predicate = predicates.get(random.nextInt(predicates.size()));
            triple.setPredicate(predicate);
        }
    }
}
