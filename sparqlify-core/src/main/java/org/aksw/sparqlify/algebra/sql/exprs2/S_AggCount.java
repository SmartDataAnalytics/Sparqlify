package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;

/**
 * Only Count(*) supported yet
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class S_AggCount
	implements SqlAggregator
{	
	public S_AggCount() {

	}

	@Override
	public SqlExpr getExpr() {
		return null;
	}
	
	public TypeToken getDatatype() {
    	return TypeToken.Long;
	}
	
	@Override
	public String toString() {
		return "COUNT(*)";
	}

	@Override
	public void asString(IndentedWriter writer) {
		String str = toString();
		writer.print(str);
	}

	@Override
	public S_AggCount copy(SqlExpr arg) {
		S_AggCount result =new  S_AggCount();
		
		return result;
	}
}
