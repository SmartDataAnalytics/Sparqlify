#!/bin/bash

#DIR="$( cd "$( dirname "$0" )" && pwd )"
DIR=`pwd`

configDirArg="$1"

if [[ "${configDirArg:0:1}" == "/" ]]; then
	configDir="$configDirArg"
else
	configDir="$DIR/$configDirArg"
fi


port="${2:-7531}"
contextPath="${3:-/}"

#cd "$DIR/../sparqlify-platform"
cd "$DIR/.."

echo "Starting Sparqlify Platform"
echo "---------------------------"
echo "Port  : $port"
echo "Config: $configDir"
echo "---------------------------"

mvn jetty:run-war "-Djetty.port=$port" "-Dplatform.linkedData.contextPath=$contextPath" "-DconfigDirectory=$configDir" 


