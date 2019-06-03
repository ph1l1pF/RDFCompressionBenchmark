import matplotlib.pyplot as plt
import statistics as st


def readLinesFromFile(file):
    f = open(file, "r")
    lines = f.readlines()
    first = lines[0]
    values =  first.split(",")
    valuesFloat = []
    for i in range(0,len(values) -1):
        valuesFloat.append(float(values[i]))
    
    return valuesFloat



# HDT

hdtValues = readLinesFromFile("/Users/philipfrerk/IdeaProjects/RDFCompressionBenchmark/starPatternResultsHDT.txt")
    
# GRP
grpValues = readLinesFromFile("/Users/philipfrerk/IdeaProjects/RDFCompressionBenchmark/starPatternResultsGRP.txt")


#if lists.__len__() == 2:
 #   assert lists[0].__len__() == lists[1].__len__()

#for i in range(lists.__len__()):
 #   plt.plot(lists[i])
    #print('std dev i: ', st.stdev(lists[i]))

#plt.plot(hdtValues)
plt.plot(grpValues)

legends = ['HDT', 'GRP']
plt.legend(legends)

plt.ylabel('compr. ratio')
plt.xlabel('<--  hub pattern           authority pattern -->')
plt.show()
