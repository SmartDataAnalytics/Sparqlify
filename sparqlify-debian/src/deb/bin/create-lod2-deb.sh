#!/bin/bash
#cd ../../..

# Note: run this from sparqlify-debian/<here> !!!
echo `pwd`

cd target/deb

if [ -d DEBIAN ]; then
    mv DEBIAN debian
fi

cd debian

if [ -f control-header ]; then
    cat control-header control > control-tmp
    mv control-tmp control
    rm control-header
fi

rm conffiles

cd ..

#mv usr debian
#mv etc debian

debuild -kE4D704B8

