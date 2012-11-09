package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public interface SqlAggregator {
	SqlExpr getExpr();
	
	/**
	 * This method must always return the same result
	 * 
	 * @return
	 */
	TypeToken getDatatype();

	void asString(IndentedWriter writer);
	
	SqlAggregator copy(SqlExpr arg);
}
