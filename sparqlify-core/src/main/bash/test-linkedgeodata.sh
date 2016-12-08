#!/bin/bash

#Iterates over a range of ids
startId=100000000
range=10000

endId=$(($startId + $range))

for ((x=$startId; x < $endId;++x)); do

    url="http://test.linkedgeodata.org/triplify/node$x"
    curl -LH "Accept: text/plain" "$url"

done



