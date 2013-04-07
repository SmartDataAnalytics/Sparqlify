package org.aksw.sparqlify.expr.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.IterableCollection;
import org.aksw.commons.factory.Factory2;
import org.aksw.commons.util.Pair;
import org.aksw.sparqlify.core.SparqlifyConstants;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class ExprUtils {
	public static boolean isConstantsOnly(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			if(!expr.isConstant()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks wtherer all arguments of the given function are constants (non-recursive).
	 * 
	 * @param fn The function to test
	 * @return True if all arguments are constants, false otherwise.
	 */
	public static boolean isConstantArgsOnly(ExprFunction fn) {
		
		boolean result = isConstantsOnly(fn.getArgs());

		return result;
	}

	
	@SuppressWarnings("unchecked")
	public static Expr andifyBalanced(Expr ... exprs) {
		return andifyBalanced(Arrays.asList(exprs));
	}
	
	@SuppressWarnings("unchecked")
	public static Expr orifyBalanced(Expr ... exprs) {
		return orifyBalanced(Arrays.asList(exprs));
	}
	
	public static List<String> extractNames(Collection<Var> vars) {
		List<String> result = new ArrayList<String>();
		for(Var var : vars) {
			result.add(var.getName());
		}
		
		return result;
	}
	
	public static Expr andifyBalanced(Iterable<Expr> exprs) {
		return opifyBalanced(exprs, new Factory2<Expr>() {
			@Override
			public Expr create(Expr a, Expr b)
			{
				return new E_LogicalAnd(a, b);
			}
		});		
	}	
	
	/**
	 * Concatenates the sub exressions using Logical_And
	 * 
	 * and(and(0, 1), and(2, 3))
	 * 
	 * @param exprs
	 * @return
	 */
	public static <T> T opifyBalanced(Iterable<T> exprs, Factory2<T> exprFactory) {
		if(exprs.iterator().hasNext() == false) { //isEmpty()) {
			return null;
		}

		List<T> current = new ArrayList<T>(IterableCollection.wrap(exprs)); 
		
		while(current.size() > 1) { 

			List<T> next = new ArrayList<T>();
			T left = null;
			for(T expr : current) {
				if(left == null) {
					left = expr;
				} else {
					T newExpr = exprFactory.create(left, expr);
					next.add(newExpr);
					left = null;
				}
			}
	
			if(left != null) {
				next.add(left);
			}
			
			current.clear();
			
			List<T> tmp = current;
			current = next;
			next = tmp;
		}
		
		return current.get(0);
	}

	public static Expr orifyBalanced(Iterable<Expr> exprs) {
		return opifyBalanced(exprs, new Factory2<Expr>() {
			@Override
			public Expr create(Expr a, Expr b)
			{
				return new E_LogicalOr(a, b);
			}
		});		
	}
	
	
	
	
	public static Pair<Var, NodeValue> extractConstantConstraint(Expr expr) {
		if(expr instanceof E_Equals) {
			E_Equals e = (E_Equals)expr;
			return extractConstantConstraint(e.getArg1(), e.getArg2());
		}
		
		return null;
	}
	
	public static Pair<Var, NodeValue> extractConstantConstraint(Expr a, Expr b) {
		Pair<Var, NodeValue> result = extractConstantConstraintDirected(a, b);
		if(result == null) {
			result = extractConstantConstraintDirected(b, a);
		}
		
		return result;
	}

	/*
	public static void extractConstantConstraints(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
		extractConstantConstraints(a, b, equiMap.getKeyToValue());
	}*/
	
	
	/**
	 * If a is a variable and b is a constant, then a mapping of the variable to the
	 * constant is put into the map, and true is returned.
	 * Otherwise, nothing is changed, and false is returned.
	 * 
	 * A mapping of a variable is set to null, if it is mapped to multiple constants
	 * 
	 * 
	 * @param a
	 * @param b
	 * @param map
	 * @return
	 */
	public static Pair<Var, NodeValue> extractConstantConstraintDirected(Expr a, Expr b) {
		if(!(a.isVariable() && b.isConstant())) {
			return null;
		}
		
		Var var = a.getExprVar().asVar();
		NodeValue nodeValue = b.getConstant();		
		
		return Pair.create(var, nodeValue);
	}

	public static Collection<? extends Expr> getSubExpressions(Expr expr, boolean reflexive) {
		Set<Expr> result = new HashSet<Expr>();
		
		if(reflexive) {
			result.add(expr);
		}
		
		getSubExpressions(expr, result);		
		
		return result;
	}
	
	public static void getSubExpressions(Expr expr, Set<Expr> result) {
		if(expr.isFunction()) {
			ExprFunction f = (ExprFunction)expr;
			
			for(int i = 1; i <= f.numArgs(); ++i) {
				Expr arg = f.getArg(i);
				if(!result.contains(arg)) {
					result.add(arg);
					getSubExpressions(arg, result);
				}
			}
		}
		
	}

	public static String getFunctionId(ExprFunction fn) {
		
		String result = null;
	
		result = fn.getOpName();
		if(result != null) {
			return result;
		}

		
		
		result = fn.getFunctionIRI();
		if(result != null) {
			return result;
		}
		
		
		FunctionLabel label = fn.getFunctionSymbol();		 
		result = label == null ? null : label.getSymbol();

		/*
		if(result != null) {
			return result;
		}*/
		
		return result;
	}
	
	/*
	public static boolean extractConstantConstraintsDirected(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
		return extractConstantConstraintsDirected(a, b, equiMap.getKeyToValue());
	}*/
	
	public static Object getJavaObject(NodeValue expr) {

		Object result;

		if(expr.equals(SparqlifyConstants.nvTypeError)) {
			result = false;
		} else if(expr.isIRI()){
			result = expr.asNode().getURI();
			//logger.debug("HACK - Uri constants should be converted to RdfTerms first");
		} else if(expr.isBoolean()) {
			result = expr.getBoolean() ? true : false;
		} else if(expr.isNumber()) {
			if(expr.isDecimal()) {
				BigDecimal d = expr.getDecimal();
				if(d.scale() > 0) {
					result = d.doubleValue();
				} else {
					result = d.intValue();
				}
			}
			else if(expr.isDouble()) {
				result = expr.getDouble();	
			} else if(expr.isFloat()) {
				result = expr.getFloat();
			} else {
				result = expr.getDecimal().longValue();
			}
		} else if(expr.isString()) {
			result = expr.getString();
		} else if(expr.isDate()) {
			result = expr.getDateTime().asCalendar();
		} else if(expr.isDateTime()) {
			result = expr.getDateTime().asCalendar();			
		}
		/*
		else if(expr instanceof NodeValueGeom){
			result = new SqlExprValue(((NodeValueGeom) expr).getGeometry());
		} else if (expr.isLiteral()) {
			Node node = expr.asNode(); 
			if(node.getLiteralDatatypeURI().equals(Vocab.wktLiteral)) {
				result = new SqlExprValue(new PGgeometry(node.getLiteralLexicalForm()), DatatypeSystemDefault._GEOMETRY);
			}
		}*/
		else {
			throw new RuntimeException("Unknow datatype of constant: " + expr.getClass() + " ," + expr);
		}

		return result;
		//return new ExprSqlBridge(result);
	}

	
	
	/*
	public static SqlDatatype getDatatype(NodeValue expr, DatatypeSystem system) {

		SqlDatatype result;

		if(expr.isIRI()){
			result = system.getByName("string")
			//logger.debug("HACK - Uri constants should be converted to RdfTerms first");
		} else if(expr.isBoolean()) {
			result = expr.getBoolean() ? true : false;
		} else if(expr.isNumber()) {
			if(expr.isDecimal()) {
				BigDecimal d = expr.getDecimal();
				if(d.scale() > 0) {
					result = d.doubleValue();
				} else {
					result = d.intValue();
				}
			}
			else if(expr.isDouble()) {
				result = expr.getDouble();	
			} else if(expr.isFloat()) {
				result = expr.getFloat();
			} else {
				result = expr.getDecimal().longValue();
			}
		} else if(expr.isString()) {
			result = expr.getString();
		} else if(expr.isDate()) {
			result = expr.getDateTime().asCalendar();
		} else if(expr.isDateTime()) {
			result = expr.getDateTime().asCalendar();			
		}
		/*
		else if(expr instanceof NodeValueGeom){
			result = new SqlExprValue(((NodeValueGeom) expr).getGeometry());
		} else if (expr.isLiteral()) {
			Node node = expr.asNode(); 
			if(node.getLiteralDatatypeURI().equals(Vocab.wktLiteral)) {
				result = new SqlExprValue(new PGgeometry(node.getLiteralLexicalForm()), DatatypeSystemDefault._GEOMETRY);
			}
		}* /
		else {
			throw new RuntimeException("Unknow datatype of constant: " + expr.getClass() + " ," + expr);
		}

		return result;
		//return new ExprSqlBridge(result);
	}*/
	
	
	public static boolean containsFalse(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			if(NodeValue.FALSE.equals(expr)) {
				return true;
			}
		}
		
		return false;
	}
}
