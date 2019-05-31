import numpy as np
import matplotlib.pyplot as plt

list1 = (1167.1160464185675,548.8165633126625,718.1417283456691,546.2280456091219,593.7391478295659,
)

labels = ['EN', 'BG', 'DE', 'FR', 'RU']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append(i)

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.35  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, list1, width, 
                color='Orange')


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Average Literal Length')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

plt.show()