#!/bin/sh
mvn -e clean install war:war deb:package
