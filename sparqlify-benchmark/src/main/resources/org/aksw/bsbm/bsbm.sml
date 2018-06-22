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


Create View view_product As
  Construct {
    ?s a bsbm:Product .
    
    ?s rdfs:label                   ?label .
    ?s rdfs:comment                 ?comment .
    ?s bsbm:producer                ?producer .
    ?s bsbm:productPropertyTextual1 ?ppt1 .
    ?s bsbm:productPropertyTextual2 ?ppt2 .
    ?s bsbm:productPropertyTextual3 ?ppt3 .
    ?s bsbm:productPropertyTextual4 ?ppt4 .
    ?s bsbm:productPropertyTextual5 ?ppt5 .
    ?s bsbm:productPropertyTextual6 ?ppt6 .
    ?s bsbm:productPropertyNumeric1 ?ppn1 .
    ?s bsbm:productPropertyNumeric2 ?ppn2 .
    ?s bsbm:productPropertyNumeric3 ?ppn3 .
    ?s bsbm:productPropertyNumeric4 ?ppn4 .
    ?s bsbm:productPropertyNumeric5 ?ppn5 .
    ?s bsbm:productPropertyNumeric6 ?ppn6 .
    
    ?s dc:publisher                 ?publisher .
    ?s dc:date                      ?publishDate .
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?nr))
    
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)
    //?producer    = uri(concat(bsbm:producer, ?producer))
    ?producer    = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Producer", ?producer))
    ?ppt1        = plainLiteral(?propertyTex1)
    ?ppt2        = plainLiteral(?propertyTex2)
    ?ppt3        = plainLiteral(?propertyTex3)
    ?ppt4        = plainLiteral(?propertyTex4)
    ?ppt5        = plainLiteral(?propertyTex5)
    ?ppt6        = plainLiteral(?propertyTex6)
    ?ppn1        = typedLiteral(?propertyNum1, xsd:Integer)
    ?ppn2        = typedLiteral(?propertyNum2, xsd:Integer)
    ?ppn3        = typedLiteral(?propertyNum3, xsd:Integer)
    ?ppn4        = typedLiteral(?propertyNum4, xsd:Integer)
    ?ppn5        = typedLiteral(?propertyNum5, xsd:Integer)
    ?ppn6        = typedLiteral(?propertyNum6, xsd:Integer)

    ?publisher   = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Producer", ?producer))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    product




Create View view_producttypeproduct As
  Construct {
    ?product a ?productType .
  }
  With
    ?product     = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?nr))
    ?productType = uri(concat(bsbm-inst:ProductType, ?producttype))
  From 
    [[SELECT a.nr, a.producer, b."producttype" FROM product a JOIN producttypeproduct b ON (b.product = a.nr)]]


Create View view_productfeatureproduct As
  Construct {
  	?product bsbm:productFeature ?feature .
  }
  With
    ?product = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?nr))
    ?feature = uri(concat(bsbm-inst:ProductFeature, ?productfeature))
  From
    [[SELECT a.nr, a.producer, b."productfeature" FROM product a JOIN productfeatureproduct b ON (b.product = a.nr)]]


Create View view_producttype As
  Construct {
    ?s a bsbm:ProductType .
    
    ?s rdfs:label      ?label .
    ?s rdfs:comment    ?comment .
    
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:ProductType, ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)

    ?publisher   = uri(concat(bsbm-inst:StandardizationInstitution, ?publisher))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    producttype


Create View view_producttype_subclasses As
  Construct {
    ?s rdfs:subClassOf ?superClass .
  }
  With
    ?s           = uri(concat(bsbm-inst:ProductType, ?nr))
    ?superClass  = uri(concat(bsbm-inst:ProductType, ?parent))
  From
    [[SELECT nr, parent FROM producttype WHERE parent IS NOT NULL]]


Create View view_productfeature As
  Construct {
    ?s a bsbm:ProductFeature .
    
    ?s rdfs:label      ?label .
    ?s rdfs:comment    ?comment .
    
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:ProductFeature, ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)

    ?publisher   = uri(concat(bsbm-inst:StandardizationInstitution, ?publisher))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    productfeature


Create View view_producer As
  Construct {
    ?s a bsbm:Producer .
    
    ?s rdfs:label      ?label .
    ?s rdfs:comment    ?comment .
    
    ?s foaf:homepage   ?homepage .
    
    ?s bsbm:country    ?country .
    
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromProducer, ?nr, "/Producer", ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)
    ?homepage    = uri(?homepage)
    ?country     = uri(concat("http://downlode.org/rdf/iso-3166/countries#", ?country))
    ?publisher   = uri(concat(bsbm-inst:dataFromProducer, ?nr, "/Producer", ?nr))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    producer


Create View view_vendor As
  Construct {
    ?s a bsbm:Vendor .
    
    ?s rdfs:label      ?label .
    ?s rdfs:comment    ?comment .
    
    ?s foaf:homepage   ?homepage .
    
    ?s bsbm:country    ?country .
    
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromVendor, ?nr, "/Vendor", ?nr))
    ?label       = plainLiteral(?label)
    ?comment     = plainLiteral(?comment)
    ?homepage    = uri(?homepage)
    ?country     = uri(concat("http://downlode.org/rdf/iso-3166/countries#", ?country))
    ?publisher   = uri(concat(bsbm-inst:dataFromVendor, ?nr, "/Vendor", ?nr))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    vendor


Create View view_offer As
  Construct {
    ?s a bsbm:Offer .

    ?s bsbm:product ?product .
    ?s bsbm:vendor  ?vendor .
    ?s bsbm:price   ?price .
    ?s bsbm:validFrom ?validFrom .
    ?s bsbm:validTo   ?validTo .
    ?s bsbm:deliveryDays ?deliveryDays .
    ?s bsbm:offerWebpage ?offerWebpage .    
    
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromVendor, ?publisher, "/Offer", ?nr))
    ?product     = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?product))    
    ?vendor      = uri(concat(bsbm-inst:dataFromVendor, ?vendor, "/Vendor", ?vendor))
    ?price       = typedLiteral(?price, bsbm:USD)    
    ?validFrom   = typedLiteral(?validFrom, xsd:dateTimeStamp)
    ?validTo     = typedLiteral(?validTo, xsd:dateTimeStamp)
    ?deliveryDays= typedLiteral(?deliveryDays, xsd:Integer)
    ?offerWebpage= uri(concat("http://vendor", ?vendor, ".com/offers/Offer", ?nr))
    ?publisher   = uri(concat(bsbm-inst:dataFromVendor, ?vendor, "/Vendor", ?vendor))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    offer


Create View view_person As
  Construct {
    ?s a foaf:Person .
    ?s foaf:name ?name .
    ?s foaf:mbox_sha1sum ?mbox .
    ?s bsbm:country ?country .
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .    
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/Reviewer", ?nr))
    ?name        = plainLiteral(?name)
    ?mbox        = plainLiteral(?mbox_sha1sum)
    ?country     = uri(concat("http://downlode.org/rdf/iso-3166/countries#", ?country))
    ?publisher   = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/RatingSite", ?publisher))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    person


Create View view_review As
  Construct {
    ?s a bsbm:Review .

    ?s bsbm:reviewFor  ?product .
    ?s rev:reviewer    ?reviewer .
    ?s bsbm:reviewDate ?date .
    ?s dc:title        ?title .
    
    ?s bsbm:rating1    ?rating1 .
    ?s bsbm:rating2    ?rating2 .
    ?s bsbm:rating3    ?rating3 .
    ?s bsbm:rating4    ?rating4 .

    ?s rev:text        ?text .
    ?s dc:publisher    ?publisher .
    ?s dc:date         ?publishDate .
  }
  With
    ?s           = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/Review", ?nr))
    ?product     = uri(concat(bsbm-inst:dataFromProducer, ?producer, "/Product", ?product))    
    ?reviewer    = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/Reviewer", ?person))
    ?date        = typedLiteral(?reviewDate, xsd:dateTimeStamp)
    ?title       = plainLiteral(?title, ?lang)
    ?text        = plainLiteral(?text, ?lang)
    ?rating1     = typedLiteral(?rating1, xsd:int)
    ?rating2     = typedLiteral(?rating2, xsd:int)
    ?rating3     = typedLiteral(?rating3, xsd:int)
    ?rating4     = typedLiteral(?rating4, xsd:int)
    ?publisher   = uri(concat(bsbm-inst:dataFromRatingSite, ?publisher, "/RatingSite", ?publisher))
    ?publishDate = typedLiteral(?publishDate, xsd:date)
  From
    [[SELECT *, upper(language) lang FROM review]]

  
