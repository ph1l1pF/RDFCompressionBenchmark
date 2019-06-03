import numpy as np
import matplotlib.pyplot as plt

# Werte der Vorkommen von relevanten properties
#list1 = (0.03229303006589987,0.07437941003379382) 
#list2 = (7.659172516304325E-4,0.0030271109007844778)
#list3 = (0.003363577301160723,0.06762709087930689)

list1 = [1,1]
list2 = (8,1)
list3 = (7,12)


labels = ['DBPedia','Wordnet']

if(labels.__len__()==0):
      for i in range(list1.__len__()):
            labels.append('DF'+str(i))

ind = np.arange(len(list1))  # the x locations for the groups
width = 0.1  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - width, list1, width, 
                color='SkyBlue', label='Symmetric')
rects2 = ax.bar(ind, list2, width,
                color='IndianRed', label='Inverse')
rects3 = ax.bar(ind + width, list3, width,
                color='Orange', label='Transitive')

def autolabel(rects, xpos='center'):
    """
    Attach a text label above each bar in *rects*, displaying its height.

    *xpos* indicates which side to place the text w.r.t. the center of
    the bar. It can be one of the following {'center', 'right', 'left'}.
    """

    xpos = xpos.lower()  # normalize the case of the parameter
    ha = {'center': 'center', 'right': 'left', 'left': 'right'}
    offset = {'center': width/2, 'right': 0, 'left': width}  # x_txt = x + w*off

    for rect in rects:
        height = int('%.0f'%rect.get_height())

        ax.text(rect.get_x()+offset[xpos] , height,
                '{}'.format(height), ha=ha[xpos], va='bottom')
        


autolabel(rects1, 'left')
autolabel(rects2, 'center')
autolabel(rects3, 'right')


# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Total number')
#ax.set_title('Relative amount')
ax.set_xticks(ind)
ax.set_xticklabels(labels)
ax.legend()

plt.show()