package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;

/**
 * Can replace columnNames in SqlExprs with other SqlExprs
 * 
 * @author raven
 *
 */
public class ColumnSubstitutor {
	private Map<String, ? extends SqlExpr> map;
    
    public ColumnSubstitutor(Map<String, ? extends SqlExpr> map)
    {
    	this.map = map;
    }

    public SqlExpr _transform(SqlExpr sqlExpr)
    {
    	return sqlExpr == null
    		? null
    		: (SqlExpr)MultiMethod.invoke(this, "transform", sqlExpr);
    }
        
    public SqlExpr transform(SqlExprFunction expr) {
    	SqlExprList args = transformList(expr.getArgs());
    	
    	SqlExpr result = SqlExprCopy.getInstance()._copy(expr, args);
    	return result;
    }
    
    protected SqlExprList transformList(Iterable<SqlExpr> sqlExprs) {
    	SqlExprList result = new SqlExprList();

    	for(SqlExpr sqlExpr : sqlExprs) {
    		result.add(_transform(sqlExpr));
    	}
    
    	return result;
    }


	public SqlExpr transform(SqlExprColumn nv) {
		SqlExpr tmp = map.get(nv.getColumnName());
		
		return tmp != null ? tmp : nv;
	}

}