# -*- coding: utf-8 -*-
"""
Created on Sat Jan 18 22:07:30 2014

@author: modified by Chuan to perform unsupervised clustering and lsa on custom data
"""
from __future__ import print_function

from sklearn.decomposition import TruncatedSVD
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import Normalizer
from sklearn import metrics
import cPickle as pickle
import re
from nltk.corpus import stopwords

from sklearn.cluster import KMeans, MiniBatchKMeans

import glob
import logging
#from optparse import OptionParser
import argparse
import sys
from time import time

import numpy as np


# Display progress logs on stdout
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(levelname)s %(message)s')

# parse commandline arguments
#op = OptionParser()
op = argparse.ArgumentParser(description='Dimensionality reduction.')
op.add_argument('language', choices=['English', 'Dutch', 'Both'], required=True,
              help="State which language to perform dim. reduction on.")          
subparsers = op.add_subparsers(title='Dataset', description='Dataset number',
                               help='State which dataset to operate on.')
parser_dataset = subparsers.add_parser('d', help='d help')
parser_dataset.add_argument('dataset', choices=['2','3','4','5'], default='',
                            help='dataset help')                               
op.add_argument("--lsa",
              dest="n_components", type=int,
              help="Preprocess documents with latent semantic analysis.")
op.add_argument("--no-minibatch",
              action="store_false", dest="minibatch", default=True,
              help="Use ordinary k-means algorithm (in batch mode).")
op.add_argument("--no-idf",
              action="store_false", dest="use_idf", default=True,
              help="Disable Inverse Document Frequency feature weighting.")
op.add_argument("--use-hashing",
              action="store_true", default=False,
              help="Use a hashing feature vectorizer")
op.add_argument("--n-features", type=int, default=None,
              help="Maximum number of features (dimensions)"
                   "to extract from text.")
op.add_argument("--verbose",
              action="store_true", dest="verbose", default=False,
              help="Print progress reports inside k-means algorithm.")

op.print_help()

opts = op.parse_args()

def output(filenames, X, output_file, components):
    f = open(output_file+'_'+components, 'w')
    i = 0
    for filename in filenames:
        f.write(filename+','+' '.join(map(str,X[i]))+'\n')
        i += 1
    f.close
    
def number_aware_tokenizer(doc):
    """ Tokenizer that maps all numeric tokens to a placeholder.

    For many applications, tokens that begin with a number are not directly
    useful, but the fact that such a token exists can be relevant.  By applying
    this form of dimensionality reduction, some methods may perform better.
    """
    token_pattern = re.compile(u'(?u)\\b\\w\\w+\\b')
    tokens = token_pattern.findall(doc)
    tokens = ["#NUMBER" if token[0] in "0123456789_" else token
              for token in tokens]
    return tokens

###############################################################################    
# state documents to read in
dataset = ''
language = op.language
#language = 'English'
#language = 'Dutch'
if language == 'English':
    extension = 'en'
    s_words = stopwords.words('english')
else:
    extension = 'nl'
    s_words = stopwords.words('dutch')
   
filenames = glob.glob('../../../../../../Testdata/dataset/'+language+dataset+'/*.'+extension)

docs = [open(f).read() for f in filenames]

# standard english stopwords - 318 words
s_words = 'english'
#s_words = [line.strip() for line in open('englishStopwords_mixed.txt')]

# Uncomment the following to do the analysis on all the DESCR
DESCR = None

print("%d documents" % len(docs))
print()

# number of clusters
n_c = 30

# Vectorizer output
##############################################################################
# Check if dataset has already been vectorized and attemps to read in
# Otherwise vectorize and dumps into a file
##############################################################################    
output_file = '../../vectorized/'+language+dataset+'.data'

#try:
#    with open(output_file+'_vect', 'rb') as handle:
#        X = pickle.load(handle)
#except IOError:
#    print("Extracting features from the training dataset using a sparse vectorizer")
#    t0 = time()
#    if opts.use_hashing:
#        if opts.use_idf:
#            # Perform an IDF normalization on the output of HashingVectorizer
#            hasher = HashingVectorizer(n_features=opts.n_features,
#                                       stop_words=s_words, non_negative=True,
#                                       norm=None, binary=False)
#            vectorizer = Pipeline((
#                ('hasher', hasher),
#                ('tf_idf', TfidfTransformer())
#            ))
#        else:
#            vectorizer = HashingVectorizer(n_features=opts.n_features,
#                                           stop_words=s_words,
#                                           non_negative=False, norm='l2',
#                                           binary=False)
#    else:
#        vectorizer = TfidfVectorizer(max_df=0.5, max_features=opts.n_features,
#                                     stop_words=s_words, use_idf=opts.use_idf)
#        #vectorizer = CountVectorizer(stop_words=stopwords)
#    with open(output_file+'_vect', 'wb') as handle:
#        X = vectorizer.fit_transform(docs)
#        pickle.dump(X, handle)
#
#print("done in %fs" % (time() - t0))
#print("n_samples: %d, n_features: %d" % X.shape)
#print()
#
#if opts.n_components:
#    print("Performing dimensionality reduction using LSA")
#    t0 = time()
#    lsa = TruncatedSVD(opts.n_components)
#    X = lsa.fit_transform(X)
#    # Vectorizer results are normalized, which makes KMeans behave as
#    # spherical k-means for better results. Since LSA/SVD results are
#    # not normalized, we have to redo the normalization.
#    X = Normalizer(copy=False).fit_transform(X)
#
#    print("done in %fs" % (time() - t0))
#    print()


###############################################################################
# Do the actual clustering

#if opts.minibatch:
#    km = MiniBatchKMeans(n_clusters=n_c, init='k-means++', n_init=1,
#                         init_size=1000, batch_size=1000, verbose=opts.verbose)
#else:
#    km = KMeans(n_clusters=n_c, init='k-means++', max_iter=100, n_init=1,
#                verbose=opts.verbose)
#
#print("Clustering sparse data with %s" % km)
#t0 = time()
#km.fit(X)
#print("done in %0.3fs" % (time() - t0))
#print()

#print("Homogeneity: %0.3f" % metrics.homogeneity_score(labels, km.labels_))
#print("Completeness: %0.3f" % metrics.completeness_score(labels, km.labels_))
#print("V-measure: %0.3f" % metrics.v_measure_score(labels, km.labels_))
#print("Adjusted Rand-Index: %.3f"
#      % metrics.adjusted_rand_score(labels, km.labels_))
#print("Silhouette Coefficient: %0.3f"
#      % metrics.silhouette_score(X, labels, sample_size=1000))

print()

#output(filenames, X, output_file, opts.n_components)
