#!/bin/bash

serviceUrl="http://localhost:9999/sparql"


queryUrl="$serviceUrl?query=Select+%2A+%7B%3Fs+%3Fp+%3Fs+.+%7D+Limit+1"


echo "[DEFAULT] $queryUrl"
curl -L "$queryUrl"

echo "[TEXT] $queryUrl"
curl -LH "Accept: text/plain" "$queryUrl"

echo "[JSON] $queryUrl"
curl -LH "Accept: application/json" "$queryUrl"

echo "[RDF/XML] $queryUrl"
curl -LH "Accept: application/rdf+xml" "$queryUrl"


