Prefix ex: <http://example.org/>
Prefix xsd: <http://www.w3.org/2001/XMLSchema#>
Create View Template test As
  Construct {
    ?s
      ex:name ?n ;
      ex:age ?a ;
      ex:gender ?g ;
      ex:email ?e ;
      ex:isPositive "true"^^xsd:boolean .
  }
  With
    ?s = uri(ex:, ?name)
    ?n = plainLiteral(?name)
    ?a = plainLiteral(?age)
    ?g = plainLiteral(?gender)
    ?e = plainLiteral(?email)

/*
Prefix ex: <http://example.org/>
Create View Template test As
  Construct {
    ?s ex:label ?o
  }
  With
    ?s = uri(ex:, ?1)
    ?o = plainLiteral(?2)
*/
