PREFIX ex: <http:/example.org/>

CREATE VIEW pokes AS
  CONSTRUCT { 
    ?s
      a ex:Poke ;
      ex:label ?l
  }
  WITH
    ?s = uri(ex:, ?foo)
    ?l = plainLiteral(?bar)
  FROM
    pokes
