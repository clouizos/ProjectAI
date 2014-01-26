# -*- coding: utf-8 -*-
"""
Created on Wed Jan 22 14:40:31 2014

@author: xiln
"""
from sklearn.utils import as_float_array
f = open('features/featureVectors_language_english_30.data', 'r')    
lines = f.read().splitlines()
nr_lines = len(lines)
labels = range(nr_lines)
features = range(nr_lines)
for i in range(nr_lines):
    labels[i], data = lines[0].split(',')
    features[i] = map(float, data.split())
X = as_float_array(features)