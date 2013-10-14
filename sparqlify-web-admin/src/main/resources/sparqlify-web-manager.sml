/**
 * Sparqlification Mapping Language file for the Sparqlify Admin schema
 *
 *
 * Note: Of course we don't want to publish the passwords
 *
 */

Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
Prefix owl: <http://www.w3.org/2002/07/owl#>
Prefix xsd: <http://www.w3.org/2001/XMLSchema#>
Prefix o: <http://example.org/ontology/>
Prefix r: <http://example.org/resource/>


Create View jdbcDataSource As
  Construct {
    ?s
      a o:JdbcDataSource ;
      o:id ?i ;
      rdfs:label ?l ;
      rdfs:comment ?c ;
      o:jdbcUrl ?ju ;
      o:username ?u ;
      .
  }
  With
    ?s = uri(r:, 'jdbcDataSource', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?l = plainLiteral(?primarylabel)
    ?c = plainLiteral(?primarycomment)
    ?ju = uri(?jdbcurl)
    ?u = plainLiteral(?username)
  From
    "jdbcdatasource"

    
Create View textResource As
  Construct {
    ?s
      a o:TextResource ;
      o:id ?i ;
      rdfs:label ?l ;
      rdfs:comment ?c ;
      o:type ?t ;
      o:format ?f ;
      o:data ?d ;
      .
  }
  With
    ?s = uri(r:, 'textResource', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?l = plainLiteral(?primarylabel)
    ?c = plainLiteral(?primarycomment)
    ?t = plainLiteral(?type)
    ?f = plainLiteral(?format)
    ?d = plainLiteral(?data)
  From
    "textresource"

    
Create View rdb2rdfConfig As
  Construct {
    ?s
      a o:Rdb2RdfConfig ;
      o:id ?i ;
      o:contextPath ?p ;
      o:dataSource ?d ;
      o:resource ?r ;
      .
  }
  With
    ?s = uri(r:, 'rdb2RdfConfig', ?id)
    ?i = typedLiteral(?id, xsd:int)
    ?p = plainLiteral(?contextpath)
    ?d = uri(r:, 'jdbcDataSource', ?jdbcdatasource_id)
    ?r = uri(r:, 'textResource', ?textresource_id)
  From
    "rdb2rdfconfig"
    
