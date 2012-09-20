#!/bin/bash
rm sp2b.tar.gz

wget http://aksw.org/files/sparqlify/sp2/sp2b.tar.gz
tar -zxvf sp2b.tar.gz

cp -r bin-ext/* sp2b/bin/

rm sp2b.tar.gz

# Copy the run-sparqlify-sp2.sh script and make it executable
cp -ui run-sparqlify-sp2.sh.dist run-sparqlify-sp2.sh
chmod +x run-sparqlify-sp2.sh


# do the same for d2r
rm d2rq-0.8.1.tar.gz
wget https://github.com/downloads/d2rq/d2rq/d2rq-0.8.1.tar.gz
tar -zxvf d2rq-0.8.1.tar.gz
mv  d2rq-0.8.1/* d2r
rmdir d2rq-0.8.1
rm d2rq-0.8.1.tar.gz


cp -ui run-d2r-sp2.sh.dist run-d2r-sp2.sh
chmod +x run-d2r-sp2.sh
