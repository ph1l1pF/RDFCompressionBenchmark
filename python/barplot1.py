import numpy as np
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties

list1 = (380,338,209,243,393,401,336,239,270,281,


)

list2 = (372,527,147,271,416,655,235,341,508,685,

)

averageLengths = (372,527,147,271,416,655,235,341,508,685,

)

relativeportions = ()

labels = ['AB_EN','AB_BG','AB_FR','AB_RU']
labels=[]

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.35  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind-width/2, list1,width,color = 'skyblue',label='Normal HDT')
rects2 = ax.bar(ind+width/2, list2, width,color='blue', label='HDT + Huffman')

fontP = FontProperties()
fontP.set_size('x-small')
#legend([plot1], "title", prop=fontP) 

fig2, ax2 = plt.subplots()
rects1 = ax2.bar(ind, averageLengths, width,color='green', label='Avg. Literal Length')

#ax3 = ax2.twinx()
#rects2 = ax3.bar(ind-width/2, averageLengths,width,color = 'green',label='y2 = Avg. Literal Length')


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Run Time [ms]')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

ax2.set_ylabel('')
#ax.set_title('Compression ratios')
ax2.set_xticks(ind)
ax2.set_xticklabels(labels)
ax2.legend()

#ax3.set_ylabel('')
#ax.set_title('Compression ratios')
#ax3.set_xticks(ind)
#ax3.set_xticklabels(labels)
#ax3.legend()

plt.show()