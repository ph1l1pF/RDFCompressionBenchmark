import numpy as np
import matplotlib.pyplot as plt

list1 = [0.754764293]
list2 = [0.50327553]

labels = ['Wordnet']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.08  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - width/2, list1, width, 
                color='Green', label='Input edge ratio')

ax2 = ax.twinx()

rects2 = ax2.bar(ind + width/2, list2, width,
                color='Orange', label='Output edge ratio')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Input edge ratio')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

# Add some text for labels, title and custom x-axis tick labels, etc.
ax2.set_ylabel('Output edge ratio')
#ax.set_title('Compression ratios')
ax2.set_xticks(ind)
ax2.set_xticklabels(labels)
ax2.legend()



plt.show()