#!/bin/bash
echo `pwd`
cat ../resources/DEBIAN/control-header ../../../target/deb/DEBIAN/control > ../../../target/deb/DEBIAN/control-tmp
mv ../../../target/deb/DEBIAN/control-tmp ../../../target/deb/DEBIAN/control
