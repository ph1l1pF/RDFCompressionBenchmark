package evaluation;

import Util.RDFTurtleConverter;
import compressionHandling.CompressionResult;
import compressionHandling.CompressionStarter;
import compressionHandling.GraphRePairStarter;
import compressionHandling.HDTStarter;

import java.io.File;

public class Test {

    public static void main(String[] agr){
        CompressionStarter graphRePairStarter = new GraphRePairStarter();
        CompressionStarter hdtStarter = new HDTStarter();

        String file = "instance-types_en_entitiybased.ttl";

//        File turtleFile = RDFTurtleConverter.convertAndStoreAsTurtleFile(file);
//        file=turtleFile.getName();

        CompressionResult resultGRP = graphRePairStarter.compress(file, null, false);
        System.out.println("grp: "+resultGRP.getCompressionRatio());

        CompressionResult resultHDT = hdtStarter.compress(file, "filess.hdt", false);
        System.out.println("hdt: "+resultHDT.getCompressionRatio());
    }
}
