# Sparqlify SPARQL->SQL rewriter

## News
16 Aug 2012 There is now the sparqlify-csv command line tool for easy transformation of CSV (excel flavour) files.

## Introduction

Sparqlify is a novel SPARQL-SQL rewriter whose development began in April 2011 in the course of the [LinkedGeoData](http://linkedgeodata.org) project.

This system's features/traits are:
* An intuitive language for expressing RDB-RDF mappings with only very little syntactic noise.
* Sparqlify will find out by itself which views it needs to select for answering a SPARQL query. (I just state this for clarity)
* SPARQL queries are rewritten into a single SQL statement, so that the underlying RDBMS has maximum control over query planning.
* Scalability: Sparqlify does not evaluate expressions in memory. All SPARQL filters end up in the corresponding SQL statement.  
* The system is a plain RDB-RDF mapper without ontological commitment (there is no need to define things as e.g. owl:Class)
* A powerful rewriting engine that analyzes filter expressions in order to eleminate self joins and joins with unsatisfiable conditions.
* Predicates can originate from database columns.
* Initial support for spatial datatypes and predicates.   
* A subset of the SPARQL 1.0 query language plus sub queries are supported.
* Currently Sparqlify only works with PostgreSQL/Postgis.  
 

## Supported SPARQL language features
* Join, LeftJoin (i.e. Optional), Union, Sub queries
* Filter predicates: comparison: (<=, <, =, >, >=) logical: (!, &&; ||) arithmetic: (+, -) spatial: st_intersects, geomFromText other: regex, lang, langMatches  
* Aggregate functions: Count(*)

NOTE: SPARQLs ternary logic (i.e. false, true and type error) is not consistently implemented yet. I plan to fix that soon. 


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
However, an example LinkedGeoData/OpenStreetMap SQL dump is located [here](http://test.linkedgeodata.org/downloads/sql-dumps/lgd_sparqlify_rc1_a.sql.bz2).
Download the file and load it into a PostgreSQL database using the following procedure:

    bzip2 -d lgd_sparqlify_rc1_a.sql.bz2
    createdb lgd_test
    psql -d lgd_test -f lgd_sparqlify_rc1_a.sql


Then run Sparqlify using

    java -cp target/sparqlify-0.0.1-SNAPSHOT-jar-with-dependencies.jar RunEndpoint -h localhost -u postgres -p secret -d lgd_test -n 1000 -t 30 -c mappings/LinkedGeoData-Triplify-IndividualViews.sparqlify

If you have configured SNORQL, you should be able to visit the web front end with your browser (e.g. `http://localhost/sparqlify/snorql`).

## Mapping Syntax:
A Sparqlify mapping configuration is a set of CREATE VIEW statements, somewhat similar to the CREATE VIEW statement from SQL.
Additionally, for convenience, prefixes can be declared, which are valid throughout the config file.
As comments, you can use //, /\* \*/, and #. 

Example:
    
    /* This is a comment
     * /* You can even nest them! */
     */
    // Prefixes are valid throughout the file
    Prefix dbp:<http://dbpedia.org/ontology/>
    Prefix ex:<http://ex.org/>

    Create View myFirstView As
        Construct {
            ?s a dbp:Person .
            ?s ex:workPage ?w .
        }
    With
        ?s = uri(concat('http://mydomain.org/person', ?id)) // Define ?s to be an URI generated from the concatenation of a prefix with mytable's id-column.
        ?w = uri(?work_page) // ?w is assigned the URIs in the column 'work_page' of 'mytable'
    Constrain
        ?w prefix "http://my-organization.org/user/"
    From
        mytable; // If you want to use an SQL query, the query (without trailing semicolon) must be enclosed in double square brackets: [[SELECT id, work_page FROM mytable]]

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

Term constructors are *blankNode(expr)*, *uri(expr)*, *plainLiteral(expr[, expr]) *, *typedLiteral(expr, expr)*.
The first argument of the term constructors is an expression denoting value of the rdf term being constructed.
The second argument of plainLiteral and typedLiteral are expressions evaluating to the language tag or datatype, respectively.
Note that the language tag is optional.

Expressions within a term constructor can make use of
* constants
* concat
* urlEncode
* urlDecode

#### Examples
    Prefix spy:<http://aksw.org/sparqlify/> // Include this prefix in your mapping file to use urlen/decode

    Prefix lgd:<http://linkedgeodata.org/triplify/>
    Prefix xsd:<http://www.w3.org/2001/XMLSchema#>

    ...
    ?a = uri(concat('http://linkedgeodata.org/triplify/node', ?node_id)
    ?b = uri(concat(lgd:node, ?node_id)) // same as above
    ?c = plainLiteral(?v, ?lang)
    ?d = typedLiteral(?age, xsd:int)
    ?e = plainLiteral(concat("http://ex.org", spy:urlEncode(?name))
    ?f = uri(spy:urlDecode(?encodedUrl))
    ?h = uri(concat(lgd:node, ?id)) // Note that uris can also be abbreviated. However, at the moment a prefix can not be used by its own.


### Constrain
If a column of a table already contains URIs, then Sparqlify cannot know what kind of URIs they are.
In order to avoid unnecessary SQL-joins (and reduce the query-rewriting time), prefix constraints can be stated.

### Further examples
Find further examples in the folder `mappings`.

**TODO** Detailed Documentation of the Sparqlify Mapping Language


## Sparqlify-CSV
sparqlify-csv is a command line tool for mapping CSV files to RDf.
The view definition syntax is almost the same as above; the differences being:

* Instead of `Create View viewname As Construct` start your views with `CREATE VIEW TEMPLATE viewname As Construct`
* There is no FROM and CONSTRAINT clause

This is an example view for creating RDF from a table with geocoded cities with schema:
(city\_name, country\_name, osm\_entity\_type, osm\_id, longitude, latitude)

Note that 'osm' stands for OpenStreetMap, and osm\_entity\_type is either 'node', 'way' or 'relation'.


    Prefix fn:<http://aksw.org/sparqlify/> //Needed for urlEncode and urlDecode.
    Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>
    Prefix owl:<http://www.w3.org/2002/07/owl#>
    Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
    Prefix geo:<http://www.w3.org/2003/01/geo/wgs84_pos#>

    Create View Template geocode As
      Construct {
        ?cityUri
          owl:sameAs ?lgdUri .

        ?lgdUri
          rdfs:label ?cityLabel ;
          geo:long ?long ;
          geo:lat ?lat .
      }
      With
        ?cityUri = uri(concat("http://fp7-pp.publicdata.eu/resource/city/", fn:urlEncode(?2), "-", fn:urlEncode(?1)))
        ?cityLabel = plainLiteral(?1)
        ?lgdUri = uri(concat("http://linkedgeodata.org/triplify/", ?4, ?5))
        ?long = typedLiteral(?6, xsd:float)
        ?lat = typedLiteral(?7, xsd:float)


### Usage
If you installed the Debian package, the following command will be system wide available:

* sparqlify-csv [options]

 Alternatively, you can also run from the JAR:

* java -cp target/sparqlify-{version}-jar-with-dependencies.jar org.aksw.sparqlify.csv.CsvMapperCliMain [options]

Options are:

* -f [file]   The CSV file to map. Currently must be excel flavour; for instance tabs will currently not work.
* -c [file]   The Sparqlify mapping file containting the 'Create View *Template* ...' statements.
* -v [view\_name]   If the mapping file (given as the -c option) contains more than one view, the name of the view to use for the mapping must be specified.
* -h   Currently not implemented. Use this flag to treat the first row of the CSV file as the headers. In this case, you can refer to the columns by name.

Example:

    sparqlify-csv -f data.csv -c mappings.sparqlify -v myview

Any RDF data will be written to STDOUT in the N-TRIPLES format. Log output is written the STDERR.


## Roadmap
The following improvements are planned (currently in no particular order):

* Support of the GRAPH keyword in the construct clause of the view definitions.
* Optimizations of LEFT-JOINS (Optional-Clauses)
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


