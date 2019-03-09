package compressionHandling;

import GraphRePair.Start;

public class GraphRePairStarter implements CompressionStarter {

    public  CompressionResult compress(String filePath){
        long compressionTime = System.currentTimeMillis();
        Start.main(new String[]{"compress",filePath, "t=rdf"});
        compressionTime = System.currentTimeMillis()-compressionTime;

        return null;
    }

    public CompressionResult decompress(String file){
        Start.main(new String[]{"decompress output/",file, "out="+ "lo"});

        return null;

    }
}
