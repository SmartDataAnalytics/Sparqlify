#!/bin/sh

debFile=`find target -name 'facete_*.deb'`
sudo dpkg -i "$debFile"
