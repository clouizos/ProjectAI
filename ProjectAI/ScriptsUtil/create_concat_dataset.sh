#!/bin/bash

nr=2
x="../DataLDA/concatenated_final$nr.data"
dir="../../Testdata/dataset/"

echo -n '' > $x
for file in $dir"English$nr/"*.en
do
    var=${file/en/nl}
    dutch=${var/"English$nr"/"Dutch$nr"}
    #echo $file | sed -e 's/\(en\)*$//g'
    var=${var##*/}
    var=${var/.nl/}
    echo -n "$var, mixed, " >> $x
    tr '\n' ' ' < $file >> $x
    tr '\n' ' ' < "$dutch" >> $x
    echo '\n' >> $x
done
