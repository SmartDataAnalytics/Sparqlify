package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.SqlDatatype;

public interface SqlAggregator {
	public SqlExpr getExpr();
	
	/**
	 * This method must always return the same result
	 * 
	 * @return
	 */
	public SqlDatatype getDatatype();
}
