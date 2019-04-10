package compressionHandling;

import GraphRePair.GraphStatistics;
import GraphRePair.Start;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphRePairStarter implements CompressionStarter {

    private int maxRank;

    public GraphRePairStarter(){
        // standard value
        maxRank = 4;
    }

    public GraphRePairStarter(int maxRank) {
        this.maxRank = maxRank;
    }

    public CompressionResult compress(String filePath, String outputName, boolean addDictionarySizeToCompressedSize) {

        // remove old compression output
        File directoryOutput = new File("output_" + filePath);

        try {
            FileUtils.deleteDirectory(directoryOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // compute the HDT dictionary size and add it the compression result size
        final long dictionarySize = getHDTDictionarySize(filePath);
        long compressedSize = 0;
        if (addDictionarySizeToCompressedSize) {
            compressedSize += dictionarySize;
        }

        // start new compression
        long compressionTime = System.currentTimeMillis();
        Start.main(new String[]{"compress", filePath, "t=rdf", "maxRank="+maxRank});
        compressionTime = System.currentTimeMillis() - compressionTime;

        File inputFile = new File(filePath);
        long originalSize = inputFile.length();


        for (File file : directoryOutput.listFiles()) {
            if (file.isFile()) {
                compressedSize += file.length();
            }
        }


        return new CompressionResult(originalSize, compressedSize, dictionarySize, compressionTime, -1, inputFile.getAbsolutePath());
    }

    public CompressionResult decompress(String file) {
        Start.main(new String[]{"decompress output/", file, "out=" + "lo"});

        return null;

    }

    private static long getHDTDictionarySize(String filePath) {
        File outputFile = new File(filePath + ".hdt");
        CompressionResult compressionResult = new HDTStarter().compress(filePath, outputFile.getAbsolutePath(), true);
        outputFile.delete();
        return compressionResult.getCompressionDictionarySize();
    }

    public static void main(String[] args){
        String s = "Array.h                      BitSequenceRG.h       HuffmanCoder.h  libcdsSDArray.h  PermutationBuilder.h       SequenceBuilderGMR.h                TextIndexCSA.h\n" +
                "BitmapsSequence.h            BitSequenceRRR.h      interface.h     libcdsTrees.h    PermutationBuilderMRRR.h   SequenceBuilder.h                   TextIndex.h\n" +
                "BitSequence375.h             BitSequenceSDArray.h  LCP_DAC.h       MapperCont.h     Permutation.h              SequenceBuilderStr.h                WaveletTree.h\n" +
                "BitSequenceBuilder375.h      BitString.h           LCP_DAC_VAR.h   Mapper.h         PermutationMRRR.h          SequenceBuilderWaveletTree.h        WaveletTreeNoptrs.h\n" +
                "BitSequenceBuilderDArray.h   Coder.h               LCP_FMN.h       MapperNone.h     PSV.h                      SequenceBuilderWaveletTreeNoptrs.h  wt_coder_binary.h\n" +
                "BitSequenceBuilder.h         comparray4.h          LCP.h           mmap.h           RMQ_succinct.h             SequenceGMRChunk.h                  wt_coder.h\n" +
                "BitSequenceBuilderRG.h       cppUtils.h            LCP_naive.h     NPR_CN.h         RMQ_succinct_lcp.h         SequenceGMR.h                       wt_coder_huff.h\n" +
                "BitSequenceBuilderRRR.h      delete_me             LCP_PhiSpare.h  NPR_FMN.h        sdarraySadakane.h          Sequence.h                          wt_node.h\n" +
                "BitSequenceBuilderSDArray.h  factorization.h       LCP_PT.h        NPR.h            SequenceAlphPart.h         SuffixTree.h                        wt_node_internal.h\n" +
                "BitSequenceDArray.h          factorization_var.h   LCP_Sad.h       NSV.h            SequenceBuilderAlphPart.h  SuffixTreeY.h                       wt_node_leaf.h\n" +
                "BitSequence.h                huff.h                libcdsBasics.h  perm.h           SequenceBuilderGMRChunk.h  TableOffsetRRR.h";

        String[] split = s.split(" ");
        List<String> realStrings = new ArrayList<>();
        for(String st : split){
            if(!st.trim().isEmpty()){
                realStrings.add(st);
            }
        }

        System.out.println(realStrings);
        System.out.println(realStrings.size());
    }
}
