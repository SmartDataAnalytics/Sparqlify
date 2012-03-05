This folder contains demonstration/testing datasets.

lgd_sparqlify_rc1.sql
---------------------
This file contains a OpenStreetMap/LinkedGeoData database about Bremen (Germany), obtained from

http://download.geofabrik.de/osm/europe/germany/bremen.osm.bz2
(The demo SQL file is obtained from the version as of 2012 January 30)

Load it into PostgreSQL using:

bzip2 -d lgd_sparqlify_rc1.sql.bz2
createdb yourdb
psql -d yourdb -f lgd_sparqlify_rc1.sql

