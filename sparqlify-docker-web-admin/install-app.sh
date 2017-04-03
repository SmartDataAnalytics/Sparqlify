#!/bin/bash

service postgresql start
apt-get -y install sparqlify-tomcat7
service postgresql stop

exit 0
