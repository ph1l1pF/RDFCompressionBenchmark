package PredicateHandling;

import Util.Triple;

import java.util.List;

public interface PredicateDistributor {

    void distributePredicates(List<Triple> triples, List<String> predicates);
}
