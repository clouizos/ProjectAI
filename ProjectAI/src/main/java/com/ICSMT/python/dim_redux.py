# -*- coding: utf-8 -*-
"""
Created on Sat Jan 18 22:07:30 2014

@author: Chuan
"""
from __future__ import print_function

from sklearn.decomposition import TruncatedSVD
from sklearn.cross_decomposition import CCA
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import Normalizer
import cPickle as pickle
import re
from nltk.corpus import stopwords
from sklearn.utils import as_float_array

import glob
import logging
import argparse
from time import time
import sys


# Display progress logs on stdout
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(levelname)s %(message)s')

# parse commandline arguments
#op = OptionParser()
op = argparse.ArgumentParser(description='Dimensionality reduction.')
op.add_argument('-l', choices=['English', 'Dutch', 'Both'], nargs='?',
                help="State which language to perform dim. reduction on.")
op.add_argument('-d', choices='2345', default='',
                help='Which dataset to operate on.')
op.add_argument('method', choices=['lsa', 'cca', '2monolsa', 'lsa2cca',
                'lda2lsa', 'lda2cca', 'bilingual2lsa'], nargs='?',
                help="State what method to run.")
op.add_argument('-c', type=int, nargs='?', help='Numbers of components of \
                the featurematrix.', default=100)
op.add_argument('-direction', choices=['e2f', 'f2e'], nargs='?',
                help="Which direction to perform cca on.")
op.add_argument('-i', nargs='+', type=argparse.FileType('r'),
                default=sys.stdin, help='Input files for \
                lda and bilangual representation methods.')
op.add_argument("-nn",
                action="store_true", default=False,
                help="Enable non-negative feature output.")
op.add_argument("-rv",
                action="store_true", default=False,
                help="Revectorize the dataset.")
op.add_argument("-df", type=float, default=0.5,
                help="Give value in range [0.7, 1) to automatically detect \
                and filter stop words based on intra corpus \
                document frequency of terms.")                              
op.add_argument("-no_idf",
                action="store_false", dest="use_idf", default=True,
                help="Disable Inverse Document Frequency feature weighting.")
op.add_argument("-use-hashing",
                action="store_true", default=False,
                help="Use a hashing feature vectorizer")
op.add_argument("-n_features", type=int, default=None,
                help="Maximum number of features (dimensions) "
                    "to extract from each document.")

op.print_help()

args = op.parse_args()

def output(filenames, X, output_file):
    f = open(output_file, 'w')
    i = 0
    for filename in filenames:
        f.write(filename+','+' '.join(map(str,X[i]))+'\n')
        i += 1
    f.close

def output2(docs, docs2, X, Y, output_file):
    f = open(output_file, 'w')
    for i in range(len(docs)):
        f.write(docs[i]+','+' '.join(map(str,X[i]))+'\n')
        f.write(docs2[i]+','+' '.join(map(str,Y[i]))+'\n')
    f.close()
    
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
    
def import_lda(f):
    lines = f.read().splitlines()
    f.close
    nr_lines = len(lines)
    labels = range(nr_lines)
    features = range(nr_lines)
    for i in range(nr_lines):
        labels[i], data = lines[i].split(',')
        features[i] = map(float, data.split())
    X = as_float_array(features)
    return labels, X

###############################################################################    
# return path and dependant variables 
def return_Path(language, dataset):
    dummywords = ['dummy1dummy', 'dummy2dummy', 'dummy3dummy', 'dummy4dummy', 
    'dummy5dummy', 'dummy6dummy', 'dummy7dummy', 'dummy8dummy', 'dummy9dummy',
    'dummy10dummy', 'dummy11dummy', 'dummy12dummy', 'dummy13dummy', 
    'dummy14dummy', 'dummy15dummy']    
    if language == 'English':
        extensions = ['en']
        s_words = stopwords.words('english') + dummywords
    elif language == 'Dutch':
        extensions = ['nl']
        s_words = stopwords.words('dutch') + dummywords
    else:
        extensions = ['en', 'nl']
        s_words = stopwords.words('english') + stopwords.words('dutch') +\
        dummywords
    
    filenames = []
    for extension in extensions:   
        filenames = filenames + glob.glob('../../../../../../Testdata/dataset/'+ \
                language+dataset+'/*.'+extension)
    
    docs = [open(f).read() for f in filenames]
    
    # standard english stopwords - 318 words
    #s_words = 'english'
    #s_words = [line.strip() for line in open('englishStopwords_mixed.txt')]
    
    print("%d documents" % len(docs))
    print()
    
    path = '../../vectorized/'+language+dataset+'.data'
    return docs, path, s_words, filenames
    
# Vectorizer output
##############################################################################
# Check if dataset has already been vectorized and attemps to read in
# Otherwise vectorize and dumps into a file
##############################################################################    
def vectorize(path, docs, features, s_words, idf, hashing, revectorize, df):    
    if df != 0.5:
        s_words=None
    t0 = time()    
    if not revectorize:
        try:
            with open(path+'_vect', 'rb') as handle:
                X = pickle.load(handle)
        except IOError:
            print("\nFile does not exist! Run with -rv to vectorize the data.")
    else:
        print("Extracting features from the training dataset using a sparse vectorizer")
        if hashing:
            if args.use_idf:
                # Perform an IDF normalization on the output of HashingVectorizer
                hasher = HashingVectorizer(n_features=features,
                                           stop_words=s_words, non_negative=True,
                                           norm=None, binary=False)
                vectorizer = Pipeline((
                    ('hasher', hasher),
                    ('tf_idf', TfidfTransformer())
                ))
            else:
                vectorizer = HashingVectorizer(n_features=features,
                                               stop_words=s_words,
                                               non_negative=False, norm='l2',
                                               binary=False)
        else:
            vectorizer = TfidfVectorizer(max_df=df, max_features=features,
                                         stop_words=s_words, use_idf=idf)
            #vectorizer = CountVectorizer(stop_words=stopwords)
        with open(path+'_vect', 'wb') as handle:
            X = vectorizer.fit_transform(docs)
            pickle.dump(X, handle)
        
        print("done in %fs" % (time() - t0))
        print("n_samples: %d, n_features: %d" % X.shape)
        print()
    return X

dataset = args.d
language = args.l
method=args.method
components=args.c
direction=args.direction
infiles=args.i
non_negative=args.nn
revectorize=args.rv
df=args.df
if(non_negative):
    nn = '_nn'
else:
    nn = ''
print("Language: %s" % language)
print("Dataset: %s" % dataset)
print("Method: %s" % method)
print("Components: %d" % components)
print("Non-negative: %s" % non_negative)

#language = 'English'
#language = 'Dutch'

if method != None:
    t0 = time()
    if method == 'lsa':
        docs, path, s_words, filenames = return_Path(language, dataset)
        X = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)
        print("Performing dimensionality reduction using LSA")
        redux = TruncatedSVD(components)  
        X = redux.fit_transform(X)
        # Vectorizer results are normalized, which makes KMeans behave as
        # spherical k-means for better results. Since LSA/SVD results are
        # not normalized, we have to redo the normalization.
        if(non_negative):        
            X = X - X.min()        
        X = Normalizer(copy=False).fit_transform(X)
        #print(X.min())
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+'_'+ \
                    language+dataset+nn+'.data'
        output(filenames, X, output_file)
    
    if method == 'cca':
        if direction == None:
            print("\nState direction to perform CCA on!")            
            raise SystemExit        
        # English set
        docs, path, s_words, filenames = return_Path('English', dataset)
        X = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)
        # Dutch set
        docs, path, s_words, filenames2 = return_Path('Dutch', dataset)
        Y = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)              
        print("Performing dimensionality reduction using CCA")
        X = X.toarray()
        Y = Y.toarray()
        if direction == 'f2e':
            X_ = Y
            Y_ = X
        else:
            X_ = X
            Y_ = Y
        cca = CCA(components)    
        cca.fit(X_, Y_)
        CCA(copy=True, max_iter=1000, n_components=components,
            scale=True, tol=1e-06)
        X, Y = cca.transform(X_, Y_)
        if(non_negative):        
            X = X - X.min()
        # Vectorizer results are normalized, which makes KMeans behave as
        # spherical k-means for better results. Since LSA/SVD results are
        # not normalized, we have to redo the normalization.
        #X_ = Normalizer(copy=False).fit_transform(X)
        #Y_ = Normalizer(copy=False).fit_transform(Y)    
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+'_'+ \
                    direction+'_d'+dataset+nn+'.data'
        output(filenames, X, output_file)
        
    if method == '2monolsa':
        # English set
        docs, path, s_words, filenames = return_Path('English', dataset)
        X = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)
        # Dutch set
        docs, path, s_words, filenames2 = return_Path('Dutch', dataset)
        Y = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)              
        print("Performing dimensionality reduction using parallel "
        "monolingual LSA")
        lsa = TruncatedSVD(components)
        lsa2 = TruncatedSVD(components)
        X = lsa.fit_transform(X)
        if(non_negative):        
            X = X - X.min()
        X = Normalizer(copy=False).fit_transform(X)
        Y = lsa2.fit_transform(Y)
        if(non_negative):        
            Y = Y - Y.min()
        Y = Normalizer(copy=False).fit_transform(Y)
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+\
            str(components)+'_d'+dataset+nn+'.data'
        output2(filenames, filenames2, X, Y, output_file)
        
    if method == 'lsa2cca':
        if direction == None:
            print("\nState direction to perform CCA on!")            
            raise SystemExit        
        # English set
        docs, path, s_words, filenames = return_Path('English', dataset)
        X = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)
        # Dutch set
        docs, path, s_words, filenames2 = return_Path('Dutch', dataset)
        Y = vectorize(path, docs, args.n_features, s_words,
                      args.use_idf, args.use_hashing, revectorize, df)              
        print("Performing dimensionality reduction using CCA after "
        "parallel monolingual LSA")
        lsa = TruncatedSVD(components)
        lsa2 = TruncatedSVD(components)
        X = lsa.fit_transform(X)
        if(non_negative):        
            X = X - X.min()
        X = Normalizer(copy=False).fit_transform(X)
        Y = lsa2.fit_transform(Y)
        if(non_negative):        
            Y = Y - Y.min()
        Y = Normalizer(copy=False).fit_transform(Y)
        if direction == 'f2e':
            X_ = Y
            Y_ = X
        else:
            X_ = X
            Y_ = Y
        cca = CCA(components)
        cca.fit(X_, Y_)
        CCA(copy=True, max_iter=1000, n_components=components,
            scale=True, tol=1e-06)
        X, Y = cca.transform(X_, Y_)
        if(non_negative):        
            X = X - X.min()
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+'_'+ \
                    direction+'_d'+dataset+nn+'.data'
        output(filenames, X, output_file)
        
    if method == 'lda2lsa':
        if len(infiles) != 2:
            print("\nlda2lsa expects 2 corresponding files!")            
            raise SystemExit
        print("Performing dimensionality reduction using LSA on LDA.")
        labels1, X = import_lda(infiles[0])
        labels2, Y = import_lda(infiles[1])
        lsa = TruncatedSVD(components)
        lsa2 = TruncatedSVD(components)
        X = lsa.fit_transform(X)
        if(non_negative):        
            X = X - X.min()
        X = Normalizer(copy=False).fit_transform(X)
        Y = lsa2.fit_transform(Y)
        if(non_negative):        
            Y = Y - Y.min()        
        Y = Normalizer(copy=False).fit_transform(Y)
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+nn+'.data'
        output2(labels1, labels2, X, Y, output_file)
        
    if method == 'lda2cca':
        if len(infiles) != 2:
            print("\nlda2cca expects 2 corresponding files!")            
            raise SystemExit
        elif direction == None:
            print("State direction to perform CCA on!")            
            raise SystemExit
        print("Performing dimensionality reduction using CCA on LDA.")
        labels1, X = import_lda(infiles[0])
        labels2, Y = import_lda(infiles[1])    
        if direction == 'f2e':
            X_ = Y
            Y_ = X
        else:
            X_ = X
            Y_ = Y
        cca = CCA(components)    
        cca.fit(X_, Y_)
        CCA(copy=True, max_iter=1000, n_components=components,
            scale=True, tol=1e-06)
        X, Y = cca.transform(X_, Y_)
        if(non_negative):        
            X = X - X.min()        
        # Vectorizer results are normalized, which makes KMeans behave as
        # spherical k-means for better results. Since LSA/SVD results are
        # not normalized, we have to redo the normalization.
        #X_ = Normalizer(copy=False).fit_transform(X)
        #Y_ = Normalizer(copy=False).fit_transform(Y)    
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+'_'+ \
                    direction+nn+'.data'
        output(labels1, X, output_file)
        
    if method == 'bilingual2lsa':
        if len(infiles) != 1:
            print("\nBilingual representation to LSA expects 1 input file!")            
            raise SystemExit
        print("Performing dimensionality reduction using LSA on "
        "Bilingual representation.")
        labels1, X = import_lda(infiles[0])
        lsa = TruncatedSVD(components)
        X = lsa.fit_transform(X)
        if(non_negative):        
            X = X - X.min()
        X = Normalizer(copy=False).fit_transform(X)
        print("done in %fs" % (time() - t0))
        print()
        output_file = '../features/'+method+'_'+str(components)+nn+'.data'
        output(labels1, X, output_file)
        
else:     
    if language == None:
        print("\nPlease input a dataset!")            
        raise SystemExit    
    t0 = time()    
    print("Vectorizing the dataset.")    
    docs, path, s_words, filenames = return_Path(language, dataset)
    X = vectorize(path, docs, args.n_features, 
                  s_words, args.use_idf, args.use_hashing, revectorize, df)
    print("done in %fs" % (time() - t0))
    print()
