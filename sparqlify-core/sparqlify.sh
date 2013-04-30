#!/bin/sh
wd=`dirname $0`
java -cp "$wd/target/sparqlify-core-jar-with-dependencies.jar" org.aksw.sparqlify.web.Main $@
