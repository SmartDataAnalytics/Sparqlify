#java -cp target/sparqlify-core-jar-with-dependencies.jar org.aksw.sparqlify.web.Main -h localhost -u postgres -p postgres -d lgd  -t 10 -c ./mappings/LinkedGeoData-Triplify-IndividualViews.sparqlify -Q 'Construct { ?s ?p ?o } { ?s ?p ?o . Filter(?p = <http://www.opengis.net/ont/geosparql#asWKT>) }'

java -cp target/sparqlify-core-jar-with-dependencies.jar org.aksw.sparqlify.web.Main -h localhost -u postgres -p postgres -d lgd  -t 10 -c ./mappings/LinkedGeoData-Triplify-IndividualViews.sparqlify -D

