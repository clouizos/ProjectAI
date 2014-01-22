#!/bin/bash


x="english_final.data"

echo -n '' > $x
for file in "English/"*.en
do
    echo -n "$file, english, " >> $x
    tr '\n' ' ' < $file >> $x
    echo '\n' >> $x
done
