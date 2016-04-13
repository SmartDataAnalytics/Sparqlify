#!/bin/bash

serviceUrl="http://localhost:9999/sparql"

queryString="Prefix geo:<http://www.georss.org/georss/> Prefix ogc:<http://www.opengis.net/ont/geosparql#> Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select Distinct * { ?a rdf:type lgdo:TramRoute . ?a lgdo:hasMember ?b . ?b a lgdo:TramStop . ?b rdfs:label ?l . ?b geo:geometry ?geo . } Limit 100"

qs=`urlencode "$queryString"`


queryUrl="$serviceUrl?query=$qs"


echo "[DEFAULT] $queryUrl"
curl -LH "Accept: text/plain" "$queryUrl"


