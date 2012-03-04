package org.aksw.sparqlify.algebra.sql.datatype;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SqlDatatypeEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(SqlDatatypeEvaluator.class);
	
	
	public static SqlDatatype eval(SqlExpr expr) {
		return (SqlDatatype)MultiMethod.invokeStatic(SqlDatatypeEvaluator.class, "_eval", expr);
	}
	
	public static SqlDatatype _eval(S_Concat expr) {
		return DatatypeSystemDefault._STRING;
		//return SqlDatatypeString.getInstance();
	}
	
	public static SqlDatatype _eval(S_LogicalAnd expr) {
		return DatatypeSystemDefault._BOOLEAN;
		//return SqlDatatypeBoolean.getInstance();
	}

	public static SqlDatatype _eval(S_LogicalOr expr) {
		return DatatypeSystemDefault._BOOLEAN;
//		return SqlDatatypeBoolean.getInstance();
	}

	public static SqlDatatype _eval(S_LogicalNot expr) {
		return DatatypeSystemDefault._BOOLEAN;
		//return SqlDatatypeBoolean.getInstance();
	}

	public static SqlDatatype _eval(S_Function expr) {
		return expr.getDatatype();
	}
	
	public static SqlDatatype _eval(SqlExprColumn expr) {
		return expr.getDatatype();
	}
	
	public static SqlDatatype _eval(SqlExprValue expr) {
		SqlDatatype result = getDatatype(expr.getObject());
		return result;
	}
	
	public static SqlDatatype getDatatype(Object o) {
		
		if(o == null) {
			throw new NullPointerException("Should not happen");
		}
		
		for(SqlDatatype datataype : DatatypeSystemDefault.getDefaultDatatypes()) {
			Class<?> correspondingClass = datataype.getCorrespondingClass();
			if(correspondingClass == null) {
				//logger.warn("Datatype '" + datataype.getName() + "' is not assigned to a native java class");
				continue;
			}
			
			if(datataype.getCorrespondingClass().isAssignableFrom(o.getClass())) {
				return datataype;
			}
		}
		
		throw new RuntimeException("Unsupported datatype: " + o + ", " + (o != null ? o.getClass() : ""));
		
		/*
		if (o instanceof Integer) {
			return DatatypeSystemDefault._INTEGER;
			//return SqlDatatypeInteger.getInstance();			
		} else if (o instanceof Long) {
			return DatatypeSystemDefault._INTEGER;
			//return SqlDatatypeBigInteger.getInstance();			
		} else if(o instanceof String) {
			return DatatypeSystemDefault._STRING;
			//return SqlDatatypeString.getInstance();
		} else if(o instanceof Boolean) {
			return DatatypeSystemDefault._BOOLEAN;
			//return SqlDatatypeBoolean.getInstance();
		} else if(o instanceop)
		} else {
			throw new RuntimeException("Unsupported datatype: " + o);
		}
		*/
	}
}
