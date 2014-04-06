package org.aksw.sparqlify.core.cast;



/**
 * An interface for looking up a mapper from NodeValue to SqlValue
 * based on an RDF datatype URI. 
 *
 * TODO Essentially converting a SPARQL constant or expression with an RDF datatype to an SQL datatype may require
 * an SQL expression - i.e. at some point in the mapping process, some interface should be extended to return an SQL expression
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

