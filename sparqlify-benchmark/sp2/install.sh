#!/bin/bash
rm sp2b.tar.gz

wget http://aksw.org/files/sparqlify/sp2/sp2b.tar.gz
tar -zxvf sp2b.tar.gz

cp -r bin-ext/* sp2b/bin/

rm sp2b.tar.gz

# Copy the run-sparqlify-sp2.sh script and make it executable
cp -ui run-sparqlify-sp2.sh.dist run-sparqlify-sp2.sh
chmod +x run-sparqlify-sp2.sh
