# -*- coding: utf-8 -*-
"""
Created on Mon Jan 27 12:21:02 2014

@author: pathos
"""

import numpy as np
import matplotlib.pyplot as plt
import csv
from decimal import Decimal


precision = []
recall = []
fmeasure = []
algorithm = []
topics = []
with open('Precision_Mono.csv','rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=',')
    for row in spamreader:
        options = row[0].split("-")
        algorithm.append(options[0])
        for w in options[1:]:
            if(w.startswith("./src")):
                topics.append(w.strip(".data")[-2:])
        precision.append(Decimal(row[1]))
        recall.append(Decimal(row[2]))
        fmeasure.append(Decimal(row[3]))
        

precision = np.asarray(precision)
recall = np.asarray(recall)
fmeasure = np.asarray(fmeasure)


algorithmrange = range(len(algorithm))
print algorithm
print
print topics

check = 0
check2 = 0
check3 = 0

'''
for k in range(len(topics)):
   
    if topics[k] == '10' and check == 0:
        check+=1
        check2 = 0
        check3 = 0
    elif topics[k] == '10' and check > 0:
        topics[k] = ""
    elif topics[k] == '20' and check2 == 0:
        check2+=1
        check = 0
        check3 = 0
    elif topics[k] == '20' and check2 >0:
        topics[k] = ""
    elif topics[k] == '30' and check3 == 0:
        check3+=1
        check2 = 0
        check = 0
    elif topics[k] == '30' and check3 > 0:
        topics[k]=""
'''    

fig = plt.figure()
ax = plt.subplot(111)
plt.ylim([0,1])
barlist = ax.bar(algorithmrange, fmeasure, width=0.00001)
ax.set_ylabel('F1-measure')
ax.set_xlabel('Topics (solid = 10, dashed = 20, dotted = 30)')
ax.set_xticks(algorithmrange)
ax.set_xticklabels("")
k = 0;
for i in algorithmrange:
    if algorithm[i] == "Kmeans":
        barlist[i].set_color('r')
        if k == 0:
            barlist[i].set_label('K-means')
            k+=1
        if topics[i] == '10':
            barlist[i].set_ls('solid')
        elif topics[i] == '20':
            barlist[i].set_ls('dashed')
        elif topics[i] == '30':
            barlist[i].set_ls('dotted')
    elif algorithm[i] == "FuzzyCmeans":
        barlist[i].set_color('g')
        if k==1:
            barlist[i].set_label("Fuzzy C-means")
            k+=1
        if topics[i] == '10':
            barlist[i].set_ls('solid')
        elif topics[i] == '20':
            barlist[i].set_ls('dashed')
        elif topics[i] == '30':
            barlist[i].set_ls('dotted')
    elif algorithm[i] == "DBScan":
        barlist[i].set_color('m')
        if k == 2:
            barlist[i].set_label("DBSCAN")
            k+=1
        if topics[i] == '10':
            barlist[i].set_ls('solid')
        elif topics[i] == '20':
            barlist[i].set_ls('dashed')
        elif topics[i] == '30':
            barlist[i].set_ls('dotted')
    elif algorithm[i] == "GMM":
        barlist[i].set_color('b')
        if k == 3:
            barlist[i].set_label("GMM with EM")
            k+=1
        if topics[i] == '10':
            barlist[i].set_ls('solid')
        elif topics[i] == '20':
            barlist[i].set_ls('dashed')
        elif topics[i] == '30':
            barlist[i].set_ls('dotted')
    elif algorithm[i] == "DPC":
        barlist[i].set_color("black")
        if k == 4:
            barlist[i].set_label("Dirichet Process")
            k+=1
        if topics[i] == '10':
            barlist[i].set_ls('solid')
        elif topics[i] == '20':
            barlist[i].set_ls('dashed')
        elif topics[i] == '30':
            barlist[i].set_ls('dotted')
        
plt.legend(loc = 3, prop={'size':8})     
plt.show()