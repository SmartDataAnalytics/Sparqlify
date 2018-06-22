CREATE VIEW myView AS
  CONSTRUCT {
    ?s a ?t
  }
  WITH
    ?s = URI(?a, ?b)
    ?t = PLAINLITERAL(?x, ?y)
  FROM
    myTable

