import matplotlib.pyplot as plt
import statistics as st
import numpy as np


def readLinesFromFile(file):
    f = open(file, "r")
    lines = f.readlines()
    listOfLists = []
    for line in lines:
        values =  line.split(",")
        valuesAsFloats = []
        for value in values:
            if(value != '\n'):
                valuesAsFloats.append(float(value))
        listOfLists.append(valuesAsFloats)

    return listOfLists

# HDT
listsHDT= readLinesFromFile("/Users/philipfrerk/IdeaProjects/RDFCompressionBenchmark/starPatternResultsHDT.txt")

# GRP
listsGRP= readLinesFromFile("/Users/philipfrerk/IdeaProjects/RDFCompressionBenchmark/starPatternResultsGRP.txt")

#colors = ['b','g','r','c','m','k']
styles = [':','--' ,'v','^','+','<','1','2']

fig, ax = plt.subplots()
indexHDTGetsBetter = 3
for i in range(listsGRP.__len__()):
    dashParam = ''
    legendGRP = ''
    legendHDT = ''
    if i > 0:
        legendGRP=''
        legendHDT=''
    if i == indexHDTGetsBetter:
        dashParam = ''
        legendGRP='GRP'
        legendHDT='HDT'
    else:
        dashParam = styles[i]
    ax.plot(listsHDT[i], dashParam, color = 'skyblue',
                label=legendHDT)
    ax.plot(listsGRP[i], dashParam, color = 'indianred',
                label=legendGRP)
    
    
plt.legend()
plt.ylabel('compr. ratio')
plt.xlabel('<--  hub pattern           authority pattern -->')
plt.show()
