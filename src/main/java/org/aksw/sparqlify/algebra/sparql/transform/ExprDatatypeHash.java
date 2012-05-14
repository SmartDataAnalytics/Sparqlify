package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Compute a hash for an expression based on their structure and
 * datatypes of the leaf nodes.
 * 
 * The hash is designed so that expressions whose inner nodes are
 * structurally equivalent, and have the same datetypes for leaves
 * yield the same hash. 
 * 
 * TODO Should be done for SQL Expressions
 * 
 * @author raven
 *
 */
public class ExprDatatypeHash {

	
	public static int hash(Expr expr, Map<String, SqlDatatype> columnToDatatype) {
		if(expr == null) {
			// Return something for null-exprs
			return 7531902;			
		}
		
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
		if(expr.isDecimal()) {
			return DatatypeSystemDefault._INTEGER.hashCode(); //SqlDatatypeInteger.getInstance().hashCode();
		} else if(expr.isString()) {
			return DatatypeSystemDefault._STRING.hashCode(); //SqlDatatypeString.getInstance().hashCode();
		} else if(expr.isIRI()) {
			// TODO: This hash approach sucks piles
			return 75319;
		} /* else if(expr instanceof NodeValueNode) {
			NodeValueNode e = (NodeValueNode)expr;
			if(e.getNode().equals(Node.NULL)) {
				return 864213;
			}
			throw new RuntimeException("Not implemented");
		} */
		else {
			throw new RuntimeException("Not implemented");
		}
	}
}
