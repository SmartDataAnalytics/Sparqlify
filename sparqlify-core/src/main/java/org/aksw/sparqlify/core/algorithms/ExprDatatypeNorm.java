package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mapping.ExprCopy;

import org.aksw.sparqlify.core.datatypes.XClass;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/**
 * Replaces variables (column references) with the names of the datatypes.
 * Used to group equivalent expressions.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprDatatypeNorm {

	//private DatatypeSystem datatypeSystem;
	private DatatypeAssigner datatypeAssigner;
	
	//public ExprDatatypeNorm(DatatypeSystem datatypeSystem) {
	public ExprDatatypeNorm(DatatypeAssigner datatypeAssigner) {
		//this.datatypeSystem = datatypeSystem;
		this.datatypeAssigner = datatypeAssigner; 
	}
	
	/*
	public DatatypeSystem getDatatypeSystem() {
		return datatypeSystem;
	}*/
	
	
	public Expr normalize(Expr expr, Map<String, XClass> typeMap) {
		
		Expr result;
		if(expr == null) {
			result = NodeValue.nvNothing;
		} else if(expr.isVariable()) {
			result = normalize(expr.getExprVar(), typeMap);
		} else if(expr.isConstant()) {
			result = normalize(expr.getConstant(), typeMap);
		} else if(expr.isFunction()) {
			result = normalize(expr.getFunction(), typeMap);
		} else {
			throw new RuntimeException("Unknown expression type: " + expr);
		}
		
		return result;
		
		//return MultiMethod.invokeStatic(ExprDatatypeNorm.class, "_nomalize", expr, columnToDatatype);
	}
	
	public List<Expr> normalizeArgs(Iterable<Expr> exprs, Map<String, XClass> columnToDatatype) {
		List<Expr> result = new ArrayList<Expr>();
		for(Expr expr : exprs) {
			Expr e = normalize(expr, columnToDatatype);
			result.add(e);
		}
		return result;
	}
	
	public Expr normalize(ExprFunction expr, Map<String, XClass> typeMap) {
		List<Expr> newArgs = normalizeArgs(expr.getArgs(), typeMap);

		return ExprCopy.getInstance().copy(expr, newArgs);
	}
	
	public ExprVar normalize(ExprVar expr, Map<String, XClass> typeMap) {
		XClass datatype = typeMap.get(expr.getVarName());
		if(datatype == null) {
			throw new RuntimeException("No datatype information for column " + expr.getVarName());
		}
		
		return new ExprVar(datatype.getName());
	}
	

	public Expr normalize(NodeValue expr, Map<String, XClass> typeMap) {
	
//		if(expr.isConstant()) {
//			System.out.println(expr);
//		}
		
		XClass datatype = datatypeAssigner.assign(expr, typeMap);
		if(datatype == null) {
			throw new RuntimeException("Could not assign datatype to constant: " + expr);
		}
		
		NodeValue result = NodeValue.makeString(datatype.getName());
		return result;
		
		
		//return NodeValue.makeString(s)
		/*
		if(expr.isDecimal()) {
			return DatatypeSystemDefault._INTEGER.hashCode(); //XClassInteger.getInstance().hashCode();
		} else if(expr.isString()) {
			return DatatypeSystemDefault._STRING.hashCode(); //XClassString.getInstance().hashCode();
		} else if(expr.isIRI()) {
			// TODO: This hash approach sucks piles
			return 75319;		
		} /* else if(expr instanceof NodeValueNode) {
			NodeValueNode e = (NodeValueNode)expr;
			if(e.getNode().equals(Node.NULL)) {
				return 864213;
			}
			throw new RuntimeException("Not implemented");
		} * /
		els e {
			throw new RuntimeException("Not implemented");
		}
		*/
	}

}
