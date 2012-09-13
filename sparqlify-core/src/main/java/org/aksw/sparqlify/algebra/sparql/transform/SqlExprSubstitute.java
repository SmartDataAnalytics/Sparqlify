package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;



public class SqlExprSubstitute {

	private Map<? extends SqlExpr, ? extends SqlExpr> map;
	    
	    public SqlExprSubstitute(Map<? extends SqlExpr, ? extends SqlExpr> map)
	    {
	    	this.map = map;
	    }

	    public SqlExpr substitute(SqlExpr expr)
	    {
	    	return expr == null
	    		? null
	    		: (SqlExpr)MultiMethod.invoke(this, "_substitute", expr);
	    }

	    public SqlExpr trySubstitute(SqlExprColumn sqlExpr) {
	    	SqlExpr substitute = map.get(sqlExpr);
	    	
	    	return (substitute == null) ? sqlExpr : substitute;
	    }

	    /*
	    protected SqlExprList _substituteList(Iterable<SqlExpr> exprs) {
	    	SqlExprList result = new SqlExprList();

	    	for(SqlExpr expr : exprs) {
	    		result.add(substitute(expr));
	    	}
	    
	    	return result;
	    }

	    public SqlExpr _substitute(SqlExpr sqlExpr) {
	    }

	    public SqlExpr trySubstitute(SqlExpr sqlExpr) {
	    	SqlExpr substitute = map.get(sqlExpr);
	    	
	    	return (substitute == null) ? sqlExpr : substitute;
	    }

	    public SqlExpr _substitute(SqlExpr1 expr) {

	    	
	    	
	    	return StringUtils.coalesce(trySubstitute(expr), copy(expr, substitute(expr.getExpr())));
	    	
	    	SqlExprList args = transformList(expr.getExpr());
	    	return result;
	    }
	    */
	    
	    
}
