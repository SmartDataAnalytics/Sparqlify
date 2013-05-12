/**
 * Sparqlify configuration for the Berlin Sparql Benchmark (BSBM)
 *
 * Changelog:
 *    0.1 (current) Initial version
 *
 * @author Claus Stadler
 *
 */

Prefix spy:<http://aksw.org/sparqlify/>

Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>
Prefix foaf:<http://xmlns.com/foaf/0.1/>
Prefix dc:<http://purl.org/dc/elements/1.1/>
Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
Prefix rev:<http://purl.org/stuff/rev#>
Prefix bsbm:<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
Prefix bsbm-inst:<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>





Create View view_producer As
  Construct {
    ?s foaf:homepage   ?homepage .
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromProducer, ?nr, "/Producer", ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)
    ?homepage    = uri(?homepage)
    ?country     = uri(concat("http://downlode.org/rdf/iso-3166/countries#", ?country))
    ?publisher   = uri(concat(bsbm-inst:dataFromProducer, ?nr, "/Producer", ?nr))
    ?publishDate = typedLiteral(?publishDate, xsd:dateTime)
  From
    producer

/*
Create View view_vendor As
  Construct {
    ?s foaf:homepage   ?homepage .
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromVendor, ?nr, "/Vendor", ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)
    ?homepage    = uri(?homepage)
    ?country     = uri(concat("http://downlode.org/rdf/iso-3166/countries#", ?country))
    ?publisher   = uri(concat(bsbm-inst:dataFromVendor, ?nr, "/Vendor", ?nr))
    ?publishDate = typedLiteral(?publishDate, xsd:dateTime)
  From
    vendor
*/

Create View view_review As
  Construct {
    ?s rev:text        ?text .
    ?s dc:title        ?title .
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/Review", ?nr))
    ?product     = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?product))    
    ?reviewer    = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/Reviewer", ?person))
    ?date        = typedLiteral(?reviewDate, xsd:dateTime)
    ?title       = plainLiteral(?title, ?lang)
    ?text        = plainLiteral(?text, ?lang)
    ?rating1     = typedLiteral(?rating1, xsd:int)
    ?rating2     = typedLiteral(?rating2, xsd:int)
    ?rating3     = typedLiteral(?rating3, xsd:int)
    ?rating4     = typedLiteral(?rating4, xsd:int)
    ?publisher   = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/RatingSite", ?publisher))
    ?publishDate = typedLiteral(?publishDate, xsd:dateTime)
  From
    [[SELECT *, upper(language) lang FROM review]]

  
