import numpy as np
import matplotlib.pyplot as plt

list1 = (
2.299313488,
1.6
)
list2 = (
0.821613439,
0.78
)

list3 = (
0.9,
0.89
)

labels = ['DBPedia','Wordnet']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.08  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind-width, list1, width, 
                color='SkyBlue', label='Input Edge Ratio')
rects2 = ax.bar(ind , list2, width,
                color='IndianRed', label='Output Edge Ratio')
rects3 = ax.bar(ind+width, list3, width, 
                color='orange', label='Size Ratio')


def autolabel(rects, xpos='center'):
    """
    Attach a text label above each bar in *rects*, displaying its height.

    *xpos* indicates which side to place the text w.r.t. the center of
    the bar. It can be one of the following {'center', 'right', 'left'}.
    """

    xpos = xpos.lower()  # normalize the case of the parameter
    ha = {'center': 'center', 'right': 'left', 'left': 'right'}
    offset = {'center': width/2, 'right': 0, 'left': width+0.01}  # x_txt = x + w*off

    for rect in rects:
        height = float('%.2f'%rect.get_height())

        ax.text(rect.get_x()+offset[xpos] , height,
                '{}'.format(height), ha=ha[xpos], va='bottom')



autolabel(rects1,'center')
autolabel(rects2,'center')
autolabel(rects3,'center')


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('')
#ax.set_title('Compression ratios')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

plt.show()