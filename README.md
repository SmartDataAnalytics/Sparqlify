# Sparqlify SPARQL->SQL rewriter

## Introduction

This is an experimental RDB-RDF mapper, which is being developed for the [LinkedGeoData](http://linkedgeodata.org) project.
Sparqlify supports a subset of the SPARQL 1.0 query language plus sub queries. A detailed description of the features is pending.
Currently only PostgreSQL is supported. We will investigate which other relational database systems can be reasonably supported.
Sparqlify rewrites a given SPARQL query into a single SQL query, thereby giving full control to the query planner of the underlying DB.

## Building

* The easiest way to build the project is to run `mvn assembly:assembly`. This will generate a single stand-alone jar containing all necessary dependencies.

## Usage

* java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint [options]

Options are:

* Server Configuration
  * -c   Sparqlify mapping definition file
  * -P   Server port [default: 7531]

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

    java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint -h localhost -u postgres -p secret -d mydb -c mydb-mappings.sparqlify -n 1000 -t 30

Agents can now access the SPARQL endpoint at `http://localhost:7531/sparql`

### Web Frontend
**TODO** This endpoint currently does NOT provide an HTML interface out of the box.
For the time being, please use e.g. [SNORQL](https://github.com/kurtjx/SNORQL) as a web frontend:

* Download Snorql
* Copy the `snorql` folder into your web server's hosting directory (e.g. `/var/www/sparqlify/snorql`)
  * Under Ubuntu you can install the Apache webserver using `sudo apt-get install apache2`, for other systems please consult the internet.
* Edit the file `snorql.js`, and set the `this._endpoint` accordingly
  * e.g. `this._endpoint = "http://localhost:7531/sparql";`

## Demo

Currently there is no publicly available online demo yet.
However, an example LinkedGeoData/OpenStreetMap SQL dump is located in this repository under `data`. 
You can load it into a PostgreSQL database using the following procedure:

    bzip2 -d lgd_sparqlify_rc1.sql.bz2
    createdb lgd_test
    psql -d lgd_test -f lgd_sparqlify_rc1.sql


Then run Sparqlify using

    java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint -h localhost -u postgres -p secret -d lgd_test -n 1000 -t 30 -c mappings/LinkedGeoData-Triplify-IndividualViews.sparqlify

If you have configured SNORQL, you should be able to visit the web front end with your browser (e.g. `http://localhost/sparqlify/snorql`).

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

### Construct
The construct clause is similar to that of a Sparql-Construct query.

### With
This clause contains a set of variable bindings that express the "glue" between the RDF and the RDB worlds.
A variable binding takes the form `?sparql_var = term-contructor(sqlExpr1, ..., sqlExprN)`.

Currently, the term constructor names are located in the `http://aksw.org/sparqlify/` namespace, and are called *blankNode*, *uri*, *plainLiteral*, *typedLiteral* and *rdfTerm*.
The term constructor rdfTerm is a generalization of the former ones, and has the signature:

    rdfTerm(termType, value, datatype, languageTag).

The available abbreviations and the relation to rdfTerm are shown below:

* blankNode(x) -> rdfTerm(0, x, '', '')
* uri(x) -> rdfTerm(1, x, '', '')
* plainLiteral(x) -> rdfTerm(2, x, '', '')
* plainLiteral(x, l) -> rdfTerm(2, x, '', l)
* typedLiteral(x, d) -> rdfTerm(3, x, d, '')

#### Examples
    Prefix spy:<http://aksw.org/sparqlify/>
    Prefix lgd:<http://linkedgeodata.org/triplify/>
    Prefix xsd:<http://www.w3.org/2001/XMLSchema#>

    ...
    ?a = spy:uri(concat('http://linkedgeodata.org/triplify/node', ?node_id)
    ?b = spy:uri(concat(lgd:node, ?node_id)) // same as above
    ?c = spy:plainLiteral(?v, ?lang)
    ?d = spy:typedLiteral(?age, xsd:int)


### Constrain
If a column of a table already contains URIs, then Sparqlify cannot know what kind of URIs they are.
In order to avoid unnecessary SQL-joins (and reduce the query-rewriting time), prefix constraints can be stated.

### Further examples
Find further examples in the folder `mappings`.

**TODO** Detailed Documentation of the Sparqlify Mapping Language

## Roadmap
The following improvements are planned (currently in no particular order):

* Get rid of the Sparqlify-namespace, and make the term-constructors first class entities.
* Support of the GRAPH keyword in the construct clause of the view definitions.
* Optimizations of LEFT-JOINS (Optional-Clauses)
* Support for the `COUNT` keyword
* Support for generic Aggregate functions
* Configurable rewrites of SPARQL->SQL predicates (so make every SQL predicate available on the Sparql level)
* Support for other relational database systems besides PostgreSQL.
* Redesign the constraints clause to support arbitrary constraint expressions; For instance ?s prefix "http://..." actually means startsWith(str(?s), 'http://...').

## Not planned yet
* SPARQL 1.1 Property Paths (This would require an extra layer of query planning within Sparqlify, as the Property Path operators do not directly map to relational operators)

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


