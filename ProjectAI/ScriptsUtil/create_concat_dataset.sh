#!/bin/bash


x="concatenated_final.data"

echo -n '' > $x
for file in "./English/"*.en
do
    var=${file#*/*/}
    var=${var%.en}
    dutch=$var".nl"
    #echo $file | sed -e 's/\(en\)*$//g'
    echo -n "$var, mixed, " >> $x
    tr '\n' ' ' < $file >> $x
    tr '\n' ' ' < "Dutch/$dutch" >> $x
    echo '\n' >> $x
done
