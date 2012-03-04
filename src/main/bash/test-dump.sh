#!/bin/bash

serviceUrl="http://localhost:9999/sparql"

limit=$1

limitStr=""
if [ ! -z "$1" ]; then
    limitStr="LIMIT $1"
fi

queryString="Select * {?s ?p ?o .} $limitStr"
qs=`urlencode "$queryString"`

queryUrl="$serviceUrl?query=$qs"


echo "[DEFAULT] $queryUrl"
curl -L "$queryUrl"

echo "[TEXT] $queryUrl"
curl -LH "Accept: text/plain" "$queryUrl"

echo "[JSON] $queryUrl"
curl -LH "Accept: application/json" "$queryUrl"

echo "[RDF/XML] $queryUrl"
curl -LH "Accept: application/rdf+xml" "$queryUrl"


