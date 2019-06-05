package evaluation;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalEvaluator {




    public static void main(String[] args){
        FileReader fi = null;
        BufferedReader bufferedReader = null;
        try {

            fi = new FileReader("mappingbased-properties_en_manyinverses.ttl");
            bufferedReader= new BufferedReader(fi);
            String line = bufferedReader.readLine();

            line=null;
            while(line!=null) {

                if (line.toLowerCase().contains("birthplace")) {
                    System.out.println(line);
                }
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getAbstracts(new String[]{"http://dbpedia.org/resource/Anton_Strashimirov"});


    }

    private static Map<String,String> getAbstracts(String[] persons){
        Model m = Util.Util.getModelFromFile("/Users/philipfrerk/Downloads/long-abstracts-en-uris_bg.nt");
        ExtendedIterator<Triple> iterator = m.getGraph().find();

        Map<String,String> mapAbstracts = new LinkedHashMap<>();
        for(String person : persons){
            mapAbstracts.put(person,null);
        }

        while(iterator.hasNext()){
            Triple triple = iterator.next();
            if(triple.getSubject().isURI()){
                for(String person : persons){
                    if(person.equals(triple.getSubject().getURI())){
                        mapAbstracts.put(person, triple.getObject().getLiteral().toString());
                    }
                }
            }
        }

        return mapAbstracts;

    }
}
