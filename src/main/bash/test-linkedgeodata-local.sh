#!/bin/bash

#Iterates over a range of ids
startId=20958816
range=10000

endId=$(($startId + $range))

for ((x=$startId; x < $endId;++x)); do

    url="http://localhost/pubby/triplify/node$x"
    curl -LH "Accept: text/plain" "$url"

done



