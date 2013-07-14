Create a DEB package:

Build all parent artifacts:

    # Go to ./&lt;repository-root&gt;
    mvn clean install

Build a deb package

    # Got to the sparqlify-debian module
    mvn clean install deb:package

    # NOTE: For LOD2 activate the LOD2 profile. Essentially this adds LOD2 stuff to the DEB to make it look official.
    mvn -P lod2 install deb:package

Build a LOD2 package

    # Run the following
    src/deb/lod2/bin/create-lod2-deb.sh
   

