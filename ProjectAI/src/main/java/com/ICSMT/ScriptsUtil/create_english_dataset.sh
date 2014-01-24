#!/bin/bash

nr=$1
x="../DataLDA/english_final$nr.data"
dir="../../Testdata/dataset/"

echo -n '' > $x
for file in $dir"English$nr/"*.en
do
    var=${file##*/}
    var=${var/.en/}
    echo -n "$file, english, " >> $x
    tr '\n' ' ' < $file >> $x
    echo '\n' >> $x
done
