#!/bin/sh

debFile=`find target -name 'sparqlify*.deb'`
sudo dpkg -i "$debFile"
