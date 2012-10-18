package org.aksw.sparqlify.core.datatypes;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_Equals
	extends SqlExprEvaluator2
{
	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		//SqlExpr result = SqlExprOps.logicalAnd(a, b);
		//return result;
		/*
		if(a.getDatatype().equals(b.getDatatype())) {
			
		}*/
		
		// TODO Rule out incompatible datatype combinations
		return S_Equals.create(a, b);
	}
}
