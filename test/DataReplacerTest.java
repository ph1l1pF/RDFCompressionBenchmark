import Ontology.DataReplacer;
import Ontology.SparqlExecutor;
import Util.Util;
import org.apache.commons.lang3.CharSet;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataReplacerTest {

    private List<String> transitives;
    private Map<String,String> inverse;

    @Before
    public void init(){
        transitives = new ArrayList<>();
        transitives.add("http://1");
        transitives.add("http://2");

        inverse = new HashMap<>();
        inverse.put("http://1","http://i1");
        inverse.put("http://2","http://i2");

    }

    private Model getModel(StringBuilder triples){
        String modelName = "model.ttl";
        try {
            new File(modelName).delete();
            Files.write(Paths.get(modelName), triples.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Util.getModelFromFile(modelName);
    }

    @Test
    public void testInverseAddEdges(){
        StringBuilder triples = new StringBuilder();
        triples.append("<http://1> <http://1> <http://2> .\n");
        triples.append("<http://1> <http://i2> <http://3> .\n");



        Model model = getModel(triples);
        Util.printModel(model);

        int count = DataReplacer.materializeInverse(inverse, model, true);
        Util.printModel(model);

        Assert.assertEquals(2,count);
    }

    @Test
    public void testTransitiveAddEdges(){
        StringBuilder triples = new StringBuilder();
        triples.append("<http://1> <http://1> <http://2> .\n");
        triples.append("<http://2> <http://1> <http://3> .\n");
        triples.append("<http://1> <http://2> <http://4> .\n");
        triples.append("<http://4> <http://2> <http://5> .\n");
        triples.append("<http://2> <http://2> <http://6> .\n");
        triples.append("<http://6> <http://2> <http://7> .\n");

        Model model = getModel(triples);

        Util.printModel(model);

        int count = DataReplacer.dematerializeTransitive(transitives, model, true);
        Assert.assertEquals(3,count);
        Util.printModel(model);
    }

    @Test
    public void testTransitiveRemoveEdges(){
        StringBuilder triples = new StringBuilder();

        triples.append("<http://1> <http://1> <http://2> .\n");
        triples.append("<http://2> <http://1> <http://3> .\n");
        triples.append("<http://1> <http://1> <http://3> .\n");

        triples.append("<http://1> <http://2> <http://4> .\n");
        triples.append("<http://4> <http://2> <http://5> .\n");
        triples.append("<http://1> <http://2> <http://5> .\n");

        triples.append("<http://2> <http://2> <http://6> .\n");
        triples.append("<http://6> <http://2> <http://7> .\n");
        triples.append("<http://2> <http://2> <http://7> .\n");

        Model model = getModel(triples);

        Util.printModel(model);

        int count = DataReplacer.dematerializeTransitive(transitives, model, false);
        Assert.assertEquals(3,count);
        Util.printModel(model);
    }

    @Test
    public void testWordnet() throws IOException {
        transitives = Files.readAllLines(Paths.get("/Users/philipfrerk/Documents/RDF_data/princeton_wordnet/transitiveProperties"));
        Model model = Util.streamRealSubModelFromFile("wordnet.nt",
                1000,1000, transitives);


        int count = DataReplacer.dematerializeTransitive(transitives, model, true);
        System.out.println(count);
        Util.writeModelToFile(new File("wordnetTransManipulated.ttl"), model);
    }

}
