#!/bin/bash


x="dutch_final.data"

echo -n '' > $x
for file in "Dutch/"*.nl
do
    echo -n "$file, dutch, " >> $x
    tr '\n' ' ' < $file >> $x
    echo '\n' >> $x
done
