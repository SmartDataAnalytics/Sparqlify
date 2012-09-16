#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"


configDir="$DIR/$1"
port="${2:-7531}"

cd "$DIR/../sparqlify-platform"

echo "Starting Sparqlify Platform"
echo "---------------------------"
echo "Port  : $port"
echo "Config: $configDir"
echo "---------------------------"

mvn jetty:run-war "-Djetty.port=$port" "-DconfigDirectory=$configDir"


