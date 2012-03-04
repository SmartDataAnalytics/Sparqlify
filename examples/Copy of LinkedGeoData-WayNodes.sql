// Prefixes
Prefix spy:<http://aksw.org/sparqlify/>
Prefix ogc:<http://www.opengis.net/rdf#>

Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>
Prefix owl:<http://www.w3.org/2002/07/owl#>


Prefix lgdn:<http://linkedgeodata.org/resource/node/>
Prefix lgdo:<http://linkedgeodata.org/ontology/>

Prefix geo:<http://www.georss.org/georss/>


Create View view_way_to_nodes As
	Construct {
		?w lgdo:hasNodeSeq ?ns .
		?w lgdo:hasNodeList ?nl .
	}
	With
		?w = spy:uri(concat('http://linkedgeodata.org/resource/way/', ?id))
		?ns = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeSeq/', ?id))  
		?nl = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?id, '/', 0))  
	From
		ways;


CREATE VIEW view_way_nodes_seq AS
	Construct {
		?wn ?p ?n .

		?n a rdf:List .
		?n rdf:first ?y .
		?n rdf:rest ?z .
	}
	With
		?wn = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeSeq/', ?way_id))
		?p  = spy:uri(concat('http://www.w3.org/1999/02/22-rdf-syntax-ns#_', ?sequence_id))
		?n = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?way_id, '/', ?sequence_id))

		?y = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id))
		?z = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?way_id, '/', ?next_sequence_id))		
	From
		[[SELECT way_id, sequence_id, node_id, sequence_id + 1 AS next_sequence_id FROM (SELECT * FROM way_nodes a WHERE sequence_id NOT IN (SELECT MAX(sequence_id) FROM way_nodes b WHERE a.way_id = b.way_id GROUP BY way_id)) x]]
		//[[SELECT way_id, sequence_id, node_id, sequence_id + 1 AS next_sequence_id FROM way_nodes]];


Create View view_way_nodes_list_nil As
	Construct {
		?wn ?p ?n .

		?n a rdf:List .
		?n rdf:first ?y .
		?n rdf:rest rdf:nil .
	}
	With
		?wn = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeSeq/', ?way_id))
		?p  = spy:uri(concat('http://www.w3.org/1999/02/22-rdf-syntax-ns#_', ?sequence_id))
		?n = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?way_id, '/', ?sequence_id))

		?y = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id))
	From
		[[SELECT way_id, sequence_id, node_id FROM (SELECT * FROM way_nodes a WHERE sequence_id IN (SELECT MAX(sequence_id) FROM way_nodes b WHERE a.way_id = b.way_id GROUP BY way_id)) x]]


		//[[SELECT way_id, MAX(sequence_id) + 1 AS sequence_id FROM way_nodes GROUP BY way_id]];
		//[[SELECT * FROM lgd_way_nodes WHERE rest_sequence_id IS NULL]];



/*
Create View view_way_nodes_list As
	Construct {
		?x a rdf:List .
		?x rdf:first ?y .
		?x rdf:rest ?z .
	}
	With
		?x = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?way_id, '/', ?sequence_id))
		?y = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id))
		?z = spy:uri(concat('http://linkedgeodata.org/resource/wayNodeList/', ?way_id, '/', ?next_sequence_id))
	From
		[[SELECT way_id, sequence_id, node_id, sequence_id + 1 AS next_sequence_id FROM (SELECT * FROM way_nodes a WHERE sequence_id NOT IN (SELECT MAX(sequence_id) FROM way_nodes b WHERE a.way_id = b.way_id GROUP BY way_id)) x]];
		//lgd_way_nodes;
*/

