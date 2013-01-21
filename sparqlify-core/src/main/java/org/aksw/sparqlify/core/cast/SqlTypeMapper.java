package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;


/**
 * An interface for looking up a mapper from NodeValue to SqlValue
 * based on an RDF datatype URI. 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface SqlTypeMapper {
	SqlDatatype getSqlDatatype(String datatypeUri);
	
	void register(String datatypeUri, SqlDatatype sqlType);
	
	//SqlValue toSql(NodeValue nodeValue);
	//SqlExpr cast(SqlExpr expr, TypeToken targetTypeToken);
}

