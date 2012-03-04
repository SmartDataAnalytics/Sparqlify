# Sparqlify SPARQL->SQL rewriter

This is an experimental RDB-RDF mapper, which is being developed for the LinkedGeoData project.
Sparqlify supports a subset of the SPARQL 1.0 query language. A detailed description of the features is pending.
Currently only PostgreSQL is supported. We will investigate which other relational database systems can be reasonably supported.
Sparqlify rewrites a given SPARQL query into a single SQL query, thereby giving full control to the query planner of the underlying DB.

Building:
* The easiest way to build the project is to run mvn assembly:assembly. This will generate a single stand-alone jar containing all necessary dependencies.

Usage:
* java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint [options]

Options are:
* Server Configuration
  * -c   Sparqlify mapping definition file
  * -P   Server port [default: 9999]

* Database Settings
  * -h   Hostname of the database (e.g. localhost or localhost:5432)
  * -d   Database name
  * -u   User name
  * -p   Password

* Quality of Service
  * -n   Maximum result set size
  * -t   Maximum query execution time in seconds (excluding rewriting time)

Example:
The following command will start the Sparqlify HTTP server on the default port.
java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint -h localhost -u postgres -p secret -d mydb -c mydb-mappings.sparqlify -n 1000 -t 30

Agents can now access the SPARQL endpoint at http://localhost:9999/sparql
TODO This endpoint does NOT provide an HTML interface. For the time being, please use SNORQL as a web frontend.

Mapping Syntax:
A Sparqlify mapping configuration is a set of CREATE VIEW statements, similar to the CREATE VIEW statement from SQL.
Additionally, for convenience, prefixes can be declared, which are valid throughout the config file.
As comments, you can use //, /\* \*/, and #. 

Example:

Prefix spy:<http://aksw.org/sparqlify/>
Prefix dbp:<http://dbpedia.org/ontology/>

Create View myFirstView As
  Construct { ?s a dbp:Person }
  With ?s = spy:uri(concat('http://mydomain.org/person', ?id)) // Define ?s to be an URI generated from the concatenation of a prefix with mytable's id-column.
  From mytable;


Find further examples in the folder 'examples'.

TODO Explain more.



