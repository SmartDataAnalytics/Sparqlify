# Sparqlify SPARQL->SQL rewriter

## Introduction

This is an experimental RDB-RDF mapper, which is being developed for the [LinkedGeoData](http://linkedgeodata.org) project.
Sparqlify supports a subset of the SPARQL 1.0 query language. A detailed description of the features is pending.
Currently only PostgreSQL is supported. We will investigate which other relational database systems can be reasonably supported.
Sparqlify rewrites a given SPARQL query into a single SQL query, thereby giving full control to the query planner of the underlying DB.

## Building

* The easiest way to build the project is to run `mvn assembly:assembly`. This will generate a single stand-alone jar containing all necessary dependencies.

## Usage

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

### Example
The following command will start the Sparqlify HTTP server on the default port.
`java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint -h localhost -u postgres -p secret -d mydb -c mydb-mappings.sparqlify -n 1000 -t 30`

Agents can now access the SPARQL endpoint at http://localhost:9999/sparql

### Web Frontend
**TODO** This endpoint currently does NOT provide an HTML interface out of the box.
For the time being, please use e.g. [SNORQL](https://github.com/kurtjx/SNORQL) as a web frontend:

* Download Snorql
* Edit the file `snorql.js`, and set the `this._endpoint` accordingly
  * e.g. `this._endpoint = "http://localhost:9999/sparql";`

## Mapping Syntax:
A Sparqlify mapping configuration is a set of CREATE VIEW statements, somewhat similar to the CREATE VIEW statement from SQL.
Additionally, for convenience, prefixes can be declared, which are valid throughout the config file.
As comments, you can use //, /\* \*/, and #. 

Example:

    Prefix spy:<http://aksw.org/sparqlify/>
    Prefix dbp:<http://dbpedia.org/ontology/>
    Prefix ex:<http://ex.org/>

    Create View myFirstView As
        Construct {
            ?s a dbp:Person .
            ?s ex:workPage ?w .
        }
    With
        ?s = spy:uri(concat('http://mydomain.org/person', ?id)) // Define ?s to be an URI generated from the concatenation of a prefix with mytable's id-column.
        ?w = spy:uri(?work_page) // ?w is assigned the URIs in the column 'work_page' of 'mytable'
    Constrain
        ?w prefix "http://my-organization.org/user/"
    From
        mytable;

A Sparqlify view definition consists of four clauses:

* *Construct*: A construct template of what RDF to construct
* *With*: A set of variable bindings which define how the variables in the construct template are generated from the underlying relation.
* *Constrain* : A set of constraints which the Sparqlify optimizer should take into account.
* *From*: The table-name from which to generate the RDF. Use double brackets if you want to specify SQL-queries (e.g. `[[SELECT id, work_page FROM mytable]]`)

Find further examples in the folder `examples`.

**TODO** Detailed Documentation of the Sparqlify Mapping Language


## Relation to R2RML
[R2RML](http://www.w3.org/2001/sw/rdb2rdf/r2rml/) is the upcoming standard language for expressing RDB-RDF mappings. A thorough analysis of the relations between R2RML and Sparqlify-ML mapping languages and their expressivity is pending.
However, many elements of R2RML can be expressed in Sparqlify-ML, so I am thinking about providing R2RML support in the future.

## Related Work
**TODO Unfinished section**
RDB-RDF mapping is an active field of research, and several mapping languages and tool implementations have been released over time:

* [Virtuoso RDF Views](http://virtuoso.openlinksw.com/whitepapers/relational%20rdf%20views%20mapping.html)
* [D2R](http://www4.wiwiss.fu-berlin.de/bizer/d2r-server/)
* [Revelytix Spyder](http://www.revelytix.com/content/spyder)
* [Asio Semantic Web Bridge for Relational Databases](http://www.bbn.com/technology/knowledge/asio_sbrd)
* [SquirrelRDF](http://jena.sourceforge.net/SquirrelRDF/)
* ODE Mapster, RDBToOnto, probably a couple more
* [Implementors or R2RML](http://www.w3.org/2001/sw/rdb2rdf/wiki/Implementations)


## Publications
Pending

### Third Party Related Publications
**TODO Unfinished section**

There are some old, but also some very recent publications which are related to Sparqlify's components:
* View candidate finding
* Query rewriting / optimization


