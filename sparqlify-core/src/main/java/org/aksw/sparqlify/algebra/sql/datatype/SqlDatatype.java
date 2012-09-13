package org.aksw.sparqlify.algebra.sql.datatype;

import com.hp.hpl.jena.graph.Node;

public interface SqlDatatype {

	/**
	 * If this datatype is a restriction, it will return the base
	 * datatype for the restriction, null otherwise.
	 * 
	 * Not sure what i mean by the comment above, but its something
	 * like: varchar(x) -> text (the x is seen as an restriction on the text)
	 * 
	 */
	SqlDatatype getBaseType();
	
	Node getXsd();
	
	public String getName();
	
	Class<?> getCorrespondingClass();
}
