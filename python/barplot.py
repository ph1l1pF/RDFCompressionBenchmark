import numpy as np
import matplotlib.pyplot as plt

list1 = (0.896,
0.9

)
list2 = (

)

labels = ['DBPedia','Wordnet']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.08  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, list1, width, 
                color='SkyBlue', label='')
rects2 = ax.bar(ind + width/2, list2, width,
                color='IndianRed', label='HDT + Huffman')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Size ratio')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

plt.show()