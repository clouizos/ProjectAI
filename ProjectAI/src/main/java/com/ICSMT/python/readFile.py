# -*- coding: utf-8 -*-
"""
Created on Sun Jan 26 12:19:27 2014

@author: ThinkPad-X220
"""

import os
from sets import Set


from sklearn.decomposition import TruncatedSVD

AllDoc = {}
voc = Set()
for dirName in os.listdir('D:/COURSES/Project AI/20132014/Intelligent Clustering/clustering_init/models/'):
    file = open('D:/COURSES/Project AI/20132014/Intelligent Clustering/clustering_init/models/'+dirName+'/model/lex.e2f', 'r')
    doc = {}    
    for t in file:  
        list = t.split(' ')
        try:
            temp = doc[list[0]+'-'+list[1]]
            doc[list[0]+'-'+list[1]] = max(temp,list[2])
        except:
            doc[list[0]+'-'+list[1]] = list[2]
        voc.add(list[0]+'-'+list[1])
    AllDoc[dirName]=doc
    file.close()

print len(AllDoc)
print len(voc)

f = open('featureVectorPairLSA.data', 'w')
for key in AllDoc:
    f.write(key)
    f.write(',')
    #f.write('english,')
    for v in voc:
        try:
            f.write(AllDoc[key][v].rstrip())
            f.write(' ')
        except:
            f.write('0.00')
            f.write(' ')
    f.write('\n')
    print 'test'
f.close()
    



#tes = Set()
#tes.add('ada')
#tes.add('beda')
#tes.add('ada')
#tes.add('bubu')
#sorted(tes,None,None,True)
#for x in tes:
    


