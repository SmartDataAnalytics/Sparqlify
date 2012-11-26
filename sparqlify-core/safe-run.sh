#!/bin/bash


#
# Executes the given command and attempts to re run it on any other exit code than 0
#
# Usage:
#    safe-run.sh command
#


limit=10

progCmd="$1"
#testCmd="$2"
sparqlServiceUri="$2"

testQuery='Select%20%2A%20%7B%20%3Chttp%3A%2F%2Fex.org%3E%20%3Chttp%3A%2F%2Fex.org%3E%20%3Chttp%3A%2F%2Fex.org%3E%20%7D%20Limit%201'
testCmd="curl --connect-timeout 30 -m 30 -o /dev/null --silent --head --write-out %{http_code} $sparqlServiceUri?query=$testQuery"


echo "$testCmd"

exit 1 

#curl http://dbpedia.org/sparql -o /dev/null --silent --head --write-out '%{http_code}'

while true; do
    echo "Performing safe run of the SPARQL endpoint $sparqlServiceUri"

    startTime=`date +%s`
    $progCmd &

    pid="$!"
    echo "Process ID is: $pid"

    while true; do
        endTime=`date +%s`
        delta=$((endTime-startTime))
        delay=$((limit-delta))

        if [ $delay -lt 0 ]; then
            delay="0"
        fi

        echo "Next test in: $delay seconds"
        sleep "$delay"

	httpMsg=`$testCmd`
	exitCode="$?"

        echo "Message: $httpMsg"

	httpCode=`echo "$httpMsg" | awk '{ print $1 }'`

	echo "Check result: Exit Code: $exitCode, HTTP Status Code: $httpCode"
        if (( exitCode > 0 || httpCode != 200 )); then
            echo "Problem Encountered. Killing the service..."
            #kill "$pid"
	    pkill -TERM -P "$pid"

            sleep 15

            echo "Forcing kill just in case"
            pkill -KILL -P "$pid"

            sleep 15

            break
	fi

	startTime=`date +%s`
    done

done

