package org.aksw.sparqlify.core;

import com.hp.hpl.jena.graph.Node;

/**
 * This class describes what a certain datatype corresponds to in xsd, java and sql.
 * 
 * TODO Rename to something like Type-Info.
 * A DatatypeSystem provides this information for TypeTokens.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
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
