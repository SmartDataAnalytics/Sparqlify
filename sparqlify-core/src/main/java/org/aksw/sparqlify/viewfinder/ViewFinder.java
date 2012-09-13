package org.aksw.sparqlify.viewfinder;

import org.aksw.commons.sparql.api.cache.core.QueryString;
import org.aksw.commons.sparql.api.cache.extra.SqlDaoBase;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;


class RdfTerm
{
	private int type;
	private String value;
	private String language;
	private String datatype;
	
	public RdfTerm(int type, String value) 
	{
		this(type, value, "", "");
	}
			
	public RdfTerm(int type, String value, String language, String datatype) {
		super();
		this.type = type;
		this.value = value;
		this.language = language;
		this.datatype = datatype;
	}
	
	public int getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getLanguage() {
		return language;
	}
	public String getDatatype() {
		return datatype;
	}
	
	
}

/*
class ShortQuad {
	private ShortRdfTerm g;
	private ShortRdfTerm s;
	private ShortRdfTerm p;
	private LongRdfTerm o;
	
	public ShortQuad(ShortRdfTerm g, ShortRdfTerm s, ShortRdfTerm p,
			LongRdfTerm o) {
		super();
		this.g = g;
		this.s = s;
		this.p = p;
		this.o = o;
	}

	public ShortRdfTerm getG() {
		return g;
	}
	public ShortRdfTerm getS() {
		return s;
	}
	public ShortRdfTerm getP() {
		return p;
	}
	public LongRdfTerm getO() {
		return o;
	}
}
*/


/**
 * Experimental class for developing my idea on
 * finding view candidates.
 * 
 * 
 * @author raven
 *
 */
public class ViewFinder
	extends SqlDaoBase
{
	private static final String text = "text";
	private static final String type = "BYTE";

	
	 enum Query
     	implements QueryString
     {
	     CREATE(
					"CREATE TABLE view_quads (" +
							
					"	v_id " + text + ",\n" + 
							
					"	g_type"  + " " + type + ",\n" +
					"	g_value" + " " + text + ",\n" +
	
					"	s_type"  + " " + type + ",\n" +
					"	s_value" + " " + text + ",\n" +
	
					"	p_type"  + " " + type + ",\n" +
					"	p_value" + " " + text + ",\n" +
	
					"	o_type"  + " " + type + ",\n" +
					"	o_value" + " " + text + ",\n" +
					"	o_language"  + " " + text + ",\n" +
					"	o_datatype" + " " + text + ",\n" +
					")"    		 
	    		 ),
	     //LOOKUP("SELECT * FROM query_cache WHERE query_hash=? LIMIT 1"),
	     INSERT("INSERT INTO view_quads VALUES(?,?,?,?,?,?,?,?,?,?,?)"),
	     //UPDATE("UPDATE query_cache SET data=?, time=? WHERE query_hash=?"),
	     ;
	
	     private String queryString;
	
	     Query(String queryString) { this.queryString = queryString; }
	     public String getQueryString() { return queryString; }
     }
	 
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
/*
	public LongRdfTerm toLong(Node node) {
		if(node.isLiteral()) {
			if(isNullOrEmpty(node.getLiteralDatatypeURI())) {
				//return new LongRdfTerm()
			}
		} else if (node.isURI()) {
			return new LongRdfTerm(1, node.getURI());
		} else if(node.isVariable()) {
			return new LongRdfTerm(4, node.getName());
		} else if (node.isBlank()) {
			return new LongRdfTerm(0, node.getBlankNodeId().getLabelString());
		} else {
			throw new NotImplementedException("Should not happen");
		}
	}
*/	

	/*
	 * v1: ?i a Amenity
	 * v2: ?j label ?k
	 * 
	 * q: ?a ?b ?c
	 * 
	 * Select * From VT v1 Where i
	 * 
	 * 
	 * Lets phrase it this way: If a query quad contains constants, we can
	 * quickly check which view quads match.
	 * 
	 * If a query contains a variable, then any view is a potential candidate, however
	 * none is, where the constants do not match. 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public static void main(String[] args) {

		
		String tableDef =
				"CREATE TABLE view_quads (" +
		
				"	v_id " + text + ",\n" + 
						
				"	g_type"  + " " + type + ",\n" +
				"	g_value" + " " + text + ",\n" +

				"	s_type"  + " " + type + ",\n" +
				"	s_value" + " " + text + ",\n" +

				"	p_type"  + " " + type + ",\n" +
				"	p_value" + " " + text + ",\n" +

				"	o_type"  + " " + type + ",\n" +
				"	o_value" + " " + text + ",\n" +
				"	o_language"  + " " + text + ",\n" +
				"	o_datatype" + " " + text + ",\n" +
				")";
				
	}
	
	
	public void insert(String view_name, QuadPattern quadPattern) {
		for(Quad quad : quadPattern) {
			
		}
	}
	
	public void insert(String viewName, Quad quad) {
		//execute(Query.INSERT, Void.class, args)
	}
}
