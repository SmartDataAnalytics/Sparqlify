#!/bin/sh
cd ..
mvn clean install

cd facete-debian-tomcat6
mvn -e clean install war:war deb:package
