#!/bin/bash

nr=$1
x="../DataLDA/dutch_final$nr.data"
dir="../../Testdata/dataset/"

echo -n '' > $x
for file in $dir"Dutch$nr/"*.nl
do
    var=${file##*/}
    var=${var/.nl/}
    echo -n "$var, dutch, " >> $x
    tr '\n' ' ' < $file >> $x
    echo '\n' >> $x
done
