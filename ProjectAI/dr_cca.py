# -*- coding: utf-8 -*-
"""
Created on Sat Jan 18 22:07:30 2014

@author: modified by Chuan to perform unsupervised clustering and cca on custom data
"""

# Author: Peter Prettenhofer <peter.prettenhofer@gmail.com>
#         Lars Buitinck <L.J.Buitinck@uva.nl>
# License: BSD 3 clause

from __future__ import print_function

from sklearn.pls import PLSCanonical, PLSRegression, CCA
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import Normalizer
from sklearn import metrics

from sklearn.cluster import KMeans, MiniBatchKMeans

import glob
import logging
from optparse import OptionParser
import sys
from time import time

import numpy as np


# Display progress logs on stdout
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(levelname)s %(message)s')

# parse commandline arguments
op = OptionParser()
op.add_option("--cca",
              dest="n_components", type="int",
              help="Preprocess documents with Canonical Correlation Analysis.")
op.add_option("--no-minibatch",
              action="store_false", dest="minibatch", default=True,
              help="Use ordinary k-means algorithm (in batch mode).")
op.add_option("--no-idf",
              action="store_false", dest="use_idf", default=True,
              help="Disable Inverse Document Frequency feature weighting.")
op.add_option("--use-hashing",
              action="store_true", default=False,
              help="Use a hashing feature vectorizer")
op.add_option("--n-features", type=int, default=10000,
              help="Maximum number of features (dimensions)"
                   "to extract from text.")
op.add_option("--verbose",
              action="store_true", dest="verbose", default=False,
              help="Print progress reports inside k-means algorithm.")

print(__doc__)
op.print_help()

(opts, args) = op.parse_args()
if len(args) > 0:
    op.error("this script takes no arguments.")
    sys.exit(1)

def output(docs, X, language, components):
    f = open('features/features_cca_'+str(components)+'.data', 'w')
    i = 0
    for filename in docs:
        f.write(filename+','+' '.join(map(str,X[i]))+'\n')
        i += 1
    f.close
    
# state documents to read in
language = 'English'
docs = glob.glob('../Testdata/dataset/'+language+'/*.en')
language = 'Dutch'
docs2 = glob.glob('../Testdata/dataset/'+language+'/*.nl')

# standard english stopwords
#stopwords = 'english'
stopwords = [line.strip() for line in open('englishStopwords_mixed.txt')]

# Uncomment the following to do the analysis on all the DESCR
DESCR = None

print("%d documents x 2" % len(docs))
print()

# number of clusters
n_c = 30

print("Extracting features from the training dataset using a sparse vectorizer")
t0 = time()
if opts.use_hashing:
    if opts.use_idf:
        # Perform an IDF normalization on the output of HashingVectorizer
        hasher = HashingVectorizer(n_features=opts.n_features,
                                   stop_words=stopwords, non_negative=True,
                                   norm=None, binary=False)
        vectorizer = Pipeline((
            ('hasher', hasher),
            ('tf_idf', TfidfTransformer())
        ))
    else:
        vectorizer = HashingVectorizer(n_features=opts.n_features,
                                       stop_words=stopwords,
                                       non_negative=False, norm='l2',
                                       binary=False)
else:
    vectorizer = TfidfVectorizer(max_df=0.5, max_features=opts.n_features,
                                 stop_words=stopwords, use_idf=opts.use_idf)
X = vectorizer.fit_transform(docs)
Y = vectorizer.fit_transform(docs2)

print("done in %fs" % (time() - t0))
print("n_samples: %d, n_features: %d x 2" % X.shape)
print()

if opts.n_components:
    print("Performing dimensionality reduction using CCA")
    t0 = time()
    cca = CCA(opts.n_components)
    # cca requires dense data
    X = X.toarray()
    Y = Y.toarray()
    cca.fit(X, Y)
    CCA(copy=True, max_iter=500, n_components=opts.n_components,
        scale=True, tol=1e-06)
    X_, Y_ = cca.transform(X, Y)
    # Vectorizer results are normalized, which makes KMeans behave as
    # spherical k-means for better results. Since LSA/SVD results are
    # not normalized, we have to redo the normalization.
    X = Normalizer(copy=False).fit_transform(X_)
    Y = Normalizer(copy=False).fit_transform(Y_)

    print("done in %fs" % (time() - t0))
    print()


###############################################################################
# Do the actual clustering

if opts.minibatch:
    km = MiniBatchKMeans(n_clusters=n_c, init='k-means++', n_init=1,
                         init_size=1000, batch_size=1000, verbose=opts.verbose)
else:
    km = KMeans(n_clusters=n_c, init='k-means++', max_iter=100, n_init=1,
                verbose=opts.verbose)

print("Clustering sparse data with %s" % km)
t0 = time()
km.fit(X)
print("done in %0.3fs" % (time() - t0))
print()

#print("Homogeneity: %0.3f" % metrics.homogeneity_score(labels, km.labels_))
#print("Completeness: %0.3f" % metrics.completeness_score(labels, km.labels_))
#print("V-measure: %0.3f" % metrics.v_measure_score(labels, km.labels_))
#print("Adjusted Rand-Index: %.3f"
#      % metrics.adjusted_rand_score(labels, km.labels_))
#print("Silhouette Coefficient: %0.3f"
#      % metrics.silhouette_score(X, labels, sample_size=1000))

print()

output(docs, X, language, opts.n_components)
