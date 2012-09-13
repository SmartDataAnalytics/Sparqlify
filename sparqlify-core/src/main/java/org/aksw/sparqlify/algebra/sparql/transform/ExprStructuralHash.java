package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Compares Expressions that are structurally equivalent, except for the variable names
 * (the datatypes of the variables must be compatible; i.e. there is a common datatype for them (e.g. int(8), int(16) -> int(32))
 * 
 * Examples
 *     concat('test', ?a : String) = concat('test', ?b : String)
 * 
 * Not equivalent
 *     concat('test', ?a : Integer) = concat('test', ?b : String)
 *     concat('test', ?a) = concat('foo', ?b)
 * 
 * @author raven
 *
 */
public class ExprStructuralHash {

	
	public static int hash(Expr expr, Map<String, SqlDatatype> columnToDatatype) {
		return (Integer)MultiMethod.invokeStatic(ExprDatatypeHash.class, "_hash", expr, columnToDatatype);
	}
	
	public static int _hashArgs(Iterable<Expr> exprs, Map<String, SqlDatatype> columnToDatatype) {
		int result = 1;
		for(Expr expr : exprs) {
			result *= (7531 + hash(expr, columnToDatatype));
		}
		return result;
	}
	
	public static int _hash(ExprFunction expr, Map<String, SqlDatatype> columnToDatatype) {
		return expr.getClass().hashCode() * _hashArgs(expr.getArgs(), columnToDatatype);
	}
	
	public static int _hash(ExprVar expr, Map<String, SqlDatatype> columnToDatatype) {
		SqlDatatype datatype = columnToDatatype.get(expr.getVarName());
		if(datatype == null) {
			throw new RuntimeException("No datatype information for column " + expr.getVarName());
		}
		
		return datatype.hashCode();
	}
	
	public static int _hash(NodeValue expr, Map<String, SqlDatatype> columnToDatatype) {
		return expr.hashCode();
		/*
		if(expr.isDecimal()) {
			return DatatypeSystemDefault._INTEGER.hashCode(); //SqlDatatypeInteger.getInstance().hashCode();
		} else if(expr.isString()) {
			return DatatypeSystemDefault._STRING.hashCode(); //SqlDatatypeString.getInstance().hashCode();
		} else if(expr.isIRI()) {
			// TODO: This hash approach sucks piles
			return 75319;
		} else {
			throw new RuntimeException("Not implemented");
		}*/
	}
}
