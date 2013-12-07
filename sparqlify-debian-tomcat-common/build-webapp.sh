#!/bin/sh

# Save the current working directory (cwd)
cwd=`pwd`

cd ../sparqlify-web-admin-server
mvn clean install

cd "$cwd"
mvn -e clean install war:war deb:package

