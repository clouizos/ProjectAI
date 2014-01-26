# -*- coding: utf-8 -*-
"""
Created on Sat Jan 18 22:07:30 2014

@author: modified by Chuan to perform unsupervised clustering and cca on custom data
"""

# Author: Peter Prettenhofer <peter.prettenhofer@gmail.com>
#         Lars Buitinck <L.J.Buitinck@uva.nl>
# License: BSD 3 clause

from __future__ import print_function

from nltk.corpus import stopwords
from sklearn.decomposition import TruncatedSVD
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
op.add_option("--cca_lsa",
              dest="n_components", type="int",
              help="Preprocess documents with Canonical Correlation Analysis on LSA.")
op.add_option("--no-minibatch",
              action="store_false", dest="minibatch", default=True,
              help="Use ordinary k-means algorithm (in batch mode).")
op.add_option("--no-idf",
              action="store_false", dest="use_idf", default=True,
              help="Disable Inverse Document Frequency feature weighting.")
op.add_option("--use-hashing",
              action="store_true", default=False,
              help="Use a hashing feature vectorizer")
op.add_option("--n-features", type=int, default=None,
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

def output(docs, docs2, X, Y, components):
    f = open('features/features_cca_lsa_'+str(components)+'.data', 'w')
    for i in range(len(docs)):
        f.write(docs[i]+','+' '.join(map(str,X[i]))+'\n')
        f.write(docs2[i]+','+' '.join(map(str,Y[i]))+'\n')
    f.close
    
# state documents to read in
language = 'English'
filenames = glob.glob('../Testdata/dataset/'+language+'/*.en')
language = 'Dutch'
filenames2 = glob.glob('../Testdata/dataset/'+language+'/*.nl')

docs = [open(f).read() for f in filenames]
docs2 = [open(f).read() for f in filenames2]

# standard english stopwords - 318 words
#stopwords = 'english'
# stopwords from nltk english: 127 dutch: 101
s_words = stopwords.words('english')
s_words2 = stopwords.words('dutch')
#stopwords = [line.strip() for line in open('englishStopwords_mixed.txt')]

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
                                   stop_words=s_words, non_negative=True,
                                   norm=None, binary=False)
        hasher2 = HashingVectorizer(n_features=opts.n_features,
                                   stop_words=s_words2, non_negative=True,
                                   norm=None, binary=False)                         
        vectorizer = Pipeline((
            ('hasher', hasher),
            ('tf_idf', TfidfTransformer())
        ))
        vectorizer2 = Pipeline((
            ('hasher', hasher),
            ('tf_idf', TfidfTransformer())
        ))
    else:
        vectorizer = HashingVectorizer(n_features=opts.n_features,
                                       stop_words=s_words,
                                       non_negative=False, norm='l2',
                                       binary=False)
        vectorizer2 = HashingVectorizer(n_features=opts.n_features,
                                       stop_words=s_words2,
                                       non_negative=False, norm='l2',
                                       binary=False)                               
else:
    vectorizer = TfidfVectorizer(max_df=0.5, max_features=opts.n_features,
                                 stop_words=s_words, use_idf=opts.use_idf)
    vectorizer2 = TfidfVectorizer(max_df=0.5, max_features=opts.n_features,
                                 stop_words=s_words2, use_idf=opts.use_idf)
X = vectorizer.fit_transform(docs)
Y = vectorizer2.fit_transform(docs2)

print("done in %fs" % (time() - t0))
print("n_samples: %d, n_features: %d x 2" % X.shape)
print()

if opts.n_components:
    print("Performing dimensionality reduction using CCA on LSA")
    print("LSA on the two corpera")
    t0 = time()
    lsa = TruncatedSVD(opts.n_components)
    lsa2 = TruncatedSVD(opts.n_components)
    X = lsa.fit_transform(X)
    X = Normalizer(copy=False).fit_transform(X)
    Y = lsa2.fit_transform(Y)
    Y = Normalizer(copy=False).fit_transform(Y)
    print("done in %fs" % (time() - t0))
    print()
    
#    print("CCA on the subspaces produced by LSA on the two corpera")
#    t0 = time()
#    cca = CCA(opts.n_components)
#    # cca requires dense data
#    cca.fit(X, Y)
#    CCA(copy=True, max_iter=1000, n_components=opts.n_components,
#        scale=True, tol=1e-06)
#    X_, Y_ = cca.transform(X, Y)
#    # Vectorizer results are normalized, which makes KMeans behave as
#    # spherical k-means for better results. Since LSA/SVD results are
#    # not normalized, we have to redo the normalization.
#    X = Normalizer(copy=False).fit_transform(X_)
#    Y = Normalizer(copy=False).fit_transform(Y_)
#
#    print("done in %fs" % (time() - t0))
#    print()


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

output(filenames, filenames2, X, Y, opts.n_components)
