package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

/**
 * Only Count(*) supported yet
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlAggregatorCount
	implements SqlAggregator
{
	public SqlAggregatorCount() {

	}

	@Override
	public SqlExpr getExpr() {
		return null;
	}
	
	public SqlDatatype getDatatype() {
    	return DatatypeSystemDefault._LONG;
	}
	
	@Override
	public String toString() {
		return "COUNT(*)";
	}
}
