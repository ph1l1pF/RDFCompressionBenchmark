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

colors = ['b','g','r','c','m','k']

fig, ax = plt.subplots()
indexHDTGetsBetter = 2
for i in range(listsGRP.__len__()):
    dashParam = ''
    legendGRP = 'GRP'
    legendHDT = 'HDT'
    if i > 0:
        legendGRP=''
        legendHDT=''
    if i == indexHDTGetsBetter:
        dashParam = '--'
    ax.plot(listsHDT[i], dashParam, color = colors[i],
                label='')
    ax.plot(listsGRP[i], dashParam, color = colors[i],
                label='')
    
    
plt.legend()
plt.ylabel('compr. ratio')
plt.xlabel('<--  hub pattern           authority pattern -->')
plt.show()
