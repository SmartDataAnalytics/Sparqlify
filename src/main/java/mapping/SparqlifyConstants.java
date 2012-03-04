package mapping;

import com.hp.hpl.jena.sparql.expr.FunctionLabel;

/**
 * Problem:
 * 
 * Create V1 { ?s label ?o2 . } ?s = Uri(id); ?o1 = Uri(uri); Select id,
 * concat("abc", u) as uri From tab1;
 * 
 * Create V2 { ?s label ?o2 . } ?s = Uri(id); ?o2 = PlainLiteral(name); Select
 * id, name From tab2;
 * 
 * 
 * Select { ?x label ?y }
 * 
 * The problem is, that ?y is mapped to both o1 and o2. However, the layout
 * differs (o1 is a Uri, o2 is a Plain Literal).
 * 
 * 
 * 
 * 
 * Resultset formats:
 * 
 * a) A separate column for each possible part of an RDF variable. s_type
 * s_value, o_type, o_value, o_lang, o_datatype, ...
 * 
 * b) A column indicating the source view (bit-pattern)
 * 
 * c) Each variable may be created from multiple columns ?s
 * 
 * Select view_1_id, view_5_id ...
 * 
 * d)
 * 
 * 
 * Create View Products { ?s a Product . ?s rdfs:label ?name . ?s x:code ?code .
 * } With ?s = Uri(Concat("http://", ?id)) ?name = ?code = Select id, name, code
 * From product;
 * 
 * 
 */

public class SparqlifyConstants {
	public static final FunctionLabel vectorLabel = new FunctionLabel("vector");
	public static final String uriLabel = "http://aksw.org/sparqlify/uri";
	public static final String rdfTermLabel = "http://aksw.org/sparqlify/rdfTerm";
	public static final String plainLiteralLabel = "http://aksw.org/sparqlify/plainLiteral";
	public static final String typedLiteralLabel = "http://aksw.org/sparqlify/typedLiteral";
	
	public static final String urlDecode = "http://aksw.org/sparqlify/urlDecode";
	public static final String urlEncode = "http://aksw.org/sparqlify/urlEncode";
	

}