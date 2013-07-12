#!/bin/bash
#cd ../../..

echo `pwd`

if [ -f target/deb/DEBIAN/control/control-header ]; then
    cat target/deb/DEBIAN/control/control-header target/deb/DEBIAN/control > target/deb/DEBIAN/control-tmp
    mv target/deb/DEBIAN/control-tmp target/deb/DEBIAN/control
    rm target/deb/DEBIAN/control/control-header
fi

cd target/deb
mv DEBIAN debian
debuild -kE4D704B8
