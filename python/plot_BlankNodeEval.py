import numpy as np
import matplotlib.pyplot as plt

list1 = (1.56379173290938,1.4342074179069217,1.4361871650007243)
list2 = (0.8950715421303657,0.7691944247578549,0.8012458351441403)
list3 = (0.8279014308426074,0.7000944956295772,0.7354773286976677)


labels = ['GC1','GC2','GC3']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.15  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - width, list1, width, 
                color='SkyBlue', label='Normal HDT')
rects2 = ax.bar(ind, list2, width,
                color='IndianRed', label='Short IDs')
rects3 = ax.bar(ind + width, list3, width,
                color='Orange', label='Omit IDs')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Compr. ratio')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

plt.show()