#!/bin/bash

serviceUrl="http://localhost:9999/sparql"

queryString="Construct {?s ?p ?o .}  {?s ?p ?o .}"
qs=`urlencode "$queryString"`

queryUrl="$serviceUrl?query=$qs"

echo "[TEXT] $queryUrl"
curl -LH "Accept: text/plain" "$queryUrl"


#echo "[DEFAULT] $queryUrl"
#curl -L "$queryUrl"

#echo "[TEXT] $queryUrl"
#curl -LH "Accept: text/plain" "$queryUrl"

#echo "[JSON] $queryUrl"
#curl -LH "Accept: application/json" "$queryUrl"

#echo "[RDF/XML] $queryUrl"
#curl -LH "Accept: application/rdf+xml" "$queryUrl"


