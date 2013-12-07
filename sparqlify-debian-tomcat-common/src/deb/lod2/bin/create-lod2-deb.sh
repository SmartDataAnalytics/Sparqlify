#!/bin/bash

echo `pwd`

cd `dirname "$0"`
cd ../../../..

echo `pwd`

# Create a lod2 folder under target
rm -rf target/lod2
mkdir -p target/lod2

# Copy the target artifacets over to it
cp -rf target/deb/* target/lod2
cp -rf src/deb/lod2/DEBIAN/* target/lod2/DEBIAN

cd target/lod2

if [ -d DEBIAN ]; then
    mv DEBIAN debian
fi

cd debian

if [ -f control-header ]; then
#    cat control-header control > control-tmp
#    mv control-tmp control
    rm control-header
fi

rm conffiles

cd ..

echo `pwd`

#mv usr debian
#mv etc debian

debuild -kE4D704B8

