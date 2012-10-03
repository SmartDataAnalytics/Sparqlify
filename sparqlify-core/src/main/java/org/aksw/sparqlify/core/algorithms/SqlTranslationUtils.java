package org.aksw.sparqlify.core.algorithms;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mapping.SparqlifyConstants;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.compile.sparql.Alignment;
import org.aksw.sparqlify.compile.sparql.SqlExprOptimizer;
import org.aksw.sparqlify.compile.sparql.SqlPrePusher;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

public class SqlTranslationUtils {

	public static final Logger logger = LoggerFactory.getLogger(SqlTranslationUtils.class);
		
	
	/*
	public static E_RdfTerm asRdfTerm(Expr expr) {
		E_RdfTerm result = null;
		
		if(expr instanceof E_RdfTerm) {
			result = (E_RdfTerm)expr; 
		}
		/*
		else if(expr instanceof E_Function) {
			// TODO Should not be needed anymore
			E_Function func = (E_Function)expr;
			if(func.getFunctionIRI().equals(SparqlifyConstants.rdfTermLabel)) {
				result = new E_RdfTerm(func.getArgs());
			}
		}* /
		
		return result;
	}*/

	
	public static final NodeValue TYPE_BLANK = NodeValue.makeInteger(0);
	public static final NodeValue TYPE_URI = NodeValue.makeInteger(1);
	public static final NodeValue TYPE_PLAIN_LITERAL = NodeValue.makeInteger(2);
	public static final NodeValue TYPE_TYPED_LITERAL = NodeValue.makeInteger(3);
	
	
	
	public static Expr getTypeOrExpr(Expr expr) {
		Expr result = null;
		
		if(expr.isConstant()) {
			Node node = expr.getConstant().getNode();
			
			if(node.isBlank()) {
				result = TYPE_BLANK;
			} else if(node.isURI()) {
				result = TYPE_URI;
			} else if(node.isLiteral()) {
				
				String datatype = node.getLiteral().getDatatypeURI();
				
				if(datatype == null || datatype.trim().isEmpty()) {
					result = TYPE_PLAIN_LITERAL;
				} else {
					result = TYPE_TYPED_LITERAL;
				}

			} else {
				
				throw new RuntimeException("Unkown node type: " + expr);
				
			}
			
		} else if(expr.isFunction()) {
			
			E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
			if(rdfTerm != null) {
				result = rdfTerm.getType();
			}

		} else {
			
			//throw new RuntimeException("Could not transform 'lang' function for: " + expr);
			result = null;
			
		}
		
		return result;
	}

	
	/**
	 * TODO: If we really want the ___lexical value___, we
	 * need to apply a transformation to value field based
	 * on its type and on the RDF datatype.
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr getLexicalValueOrExpr(Expr expr) {
		
		throw new RuntimeException("Not implemented yet");
		/*
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getLexicalValue() : expr;
		return result;
		*/
	}
	
	public static Expr getDatatypeOrExpr(Expr expr) {
		Expr result = null;
		
		if(expr.isConstant()) {
			Node node = expr.getConstant().getNode();
			if(node.isLiteral()) {
				String datatype = node.getLiteralDatatypeURI();
				
				// Prevent null values
				datatype = datatype == null ? "" : datatype;
				
				result = NodeValue.makeString(datatype);
				
			} else {
				result = NodeValue.nvNothing;
			}
		} else if(expr.isFunction()) {
			
			E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
			if(rdfTerm != null) {
				result = rdfTerm.getDatatype();
			}

		} else {
			
			//throw new RuntimeException("Could not transform 'lang' function for: " + expr);
			result = null;
			
		}
		
		return result;
	}

	public static Expr extractLanguageTag(Expr expr) {
		
		Expr result = null;
		
		if(expr.isConstant()) {
			Node node = expr.getConstant().getNode();
			if(node.isLiteral()) {
				String lang = node.getLiteralLanguage();
				
				// Prevent null values
				lang = lang == null ? "" : lang;
				
				result = NodeValue.makeString(lang);
				
			} else {
				result = NodeValue.nvNothing;
			}
		} else if(expr.isFunction()) {
			
			E_RdfTerm rdfTerm = expandRdfTerm(expr.getFunction());
			if(rdfTerm != null) {
				result = rdfTerm.getLanguageTag();
			}

		} else {
			
			//throw new RuntimeException("Could not transform 'lang' function for: " + expr);
			result = null;
			
		}
		
		return result;
	}


	
	public static E_RdfTerm expandRdfTerm(Expr expr) {
		
		E_RdfTerm result = null;
		
		if(expr.isFunction()) {
			result = expandRdfTerm(expr.getFunction());
		}
		
		return result;
	}
	
	
	/**
	 * Expands blankNode, uri, plainLiteral and typedLiteral to E_RdfTerm. 
	 * Returns the expr if it already is of type E_RdfTerm.
	 * 
	 * Return null for any other function, variable or constant.
	 * 
	 * @param expr
	 * @return
	 */
	public static E_RdfTerm expandRdfTerm(ExprFunction expr) {

		if(expr instanceof E_RdfTerm) {
			return (E_RdfTerm)expr;
		} else if(SparqlifyConstants.rdfTermLabel.equals(expr.getFunctionIRI())) {
			if(expr.getArgs().size() != 4) {
				throw new RuntimeException("RdfTerm requires 4 arguments, instead got: " + expr);
			}
			
			return new E_RdfTerm(
					expr.getArg(1), expr.getArg(2), expr.getArg(3), expr.getArg(4));			
		} else if (SparqlifyConstants.uriLabel.equals(expr.getFunctionIRI())) {
			return new E_RdfTerm(
					NodeValue.makeDecimal(1), expr.getArgs().get(0),
					NodeValue.makeString(""), NodeValue.makeString(""));
		} else if (SparqlifyConstants.plainLiteralLabel.equals(expr.getFunctionIRI())) {
			// The second argument is optional
			// If it is null, "", or not present, it will be treated as ""
			Expr lang = NodeValue.makeString("");

			if (expr.getArgs().size() == 2) {
				Expr tmp = expr.getArgs().get(1);
				if (tmp != null) {
					lang = tmp;
				}
			}

			return new E_RdfTerm(
					NodeValue.makeDecimal(2), expr.getArgs().get(0), lang,
					NodeValue.makeString(""));
		} else if (SparqlifyConstants.typedLiteralLabel.equals(expr.getFunctionIRI())) {
			return new E_RdfTerm(
					NodeValue.makeDecimal(3), expr.getArgs().get(0),
					NodeValue.makeString(""), expr.getArgs().get(1));
		} else if (SparqlifyConstants.blankNodeLabel.equals(expr.getFunctionIRI())) {
			return new E_RdfTerm(
					NodeValue.makeDecimal(0), expr.getArgs().get(0),
					NodeValue.makeString(""), NodeValue.makeString(""));			
		}

		return null;
	}

	public static E_RdfTerm expandConstant(Expr expr) {
		
		E_RdfTerm result = null;
		
		if(expr.isConstant()) {
			
			result = expandConstant(expr.getConstant());
			
		}
		
		return result;
	}
	
	public static E_RdfTerm expandConstant(Node node) {
		int type;
		Object lex = "";
		String lang = "";
		String dt = "";
		
		if(node.isBlank()) {
			type = 0;
			lex = node.getBlankNodeId().getLabelString();
		} else if(node.isURI()) {
			type = 1;
			lex = node.getURI();
		} else if(node.isLiteral()) {
			
			lex = node.getLiteral().getValue();
			
			//lex = node.getLiteralLexicalForm();
	
			String datatype = node.getLiteralDatatypeURI();
			if(datatype == null || datatype.isEmpty()) {
				type = 2;
				lang = node.getLiteralLanguage();
			} else {
				type = 3;
				dt = node.getLiteralDatatypeURI();
			}
		} else {
			throw new RuntimeException("Should not happen");
		}
	
		return new E_RdfTerm(
				NodeValue.makeDecimal(type), NodeValue.makeNode(lex.toString(), lang, dt),
				NodeValue.makeString(lang), NodeValue.makeString(dt));
		
		/*
		return new E_Function(SparqlifyConstants.rdfTermLabel, SparqlSubstitute.makeExprList(
				NodeValue.makeDecimal(type), NodeValue.makeNode(lex.toString(), lang, dt),
				NodeValue.makeString(lang), NodeValue.makeString(dt)));
		*/
	
	}


	/**
	 * Converts op(f(argsA), f(argsB)) -> op1(op2(argsA[1], argsB[2]), ...,
	 * op2(...)) This is mainly used for translating Equals(rdfTerm(argsA),
	 * rdfTerm(argsB)) to And(Equals(argsA[0], argsB[0]), Equals(...), ...)
	 * 
	 * Example: f(argsA) = f(argsB) -> argsA = argsB -> argsA.1 = argsB.1 &&
	 * argsA.2 = argsB.2 && ...
	 * 
	 * TODO: How to account for extra information that might be available on the
	 * constraints on the variables, such as argX.y is a constant?
	 * 
	 * Note: This does not work it many cases for e.g. concat : concat("ab",
	 * "c") = concat("a", "bc")
	 * 
	 * 
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr optimizeRdfTerm(E_Equals expr) {
		Expr result = expr;
	
		// TODO Check if the arguments are simple or functions
		// An expression such as f(vector1) = f(vector2) can be translated into
		// vector1 = vector2 iff
		// . f is stateless/deterministic.
		// . f is not overloaded (we do not take that case into account)
	
		Expr a = expr.getArg1();
		Expr b = expr.getArg2();
	
		if (a.isFunction() && b.isFunction()) {
			ExprFunction fa = a.getFunction();
			ExprFunction fb = b.getFunction();
	
			FunctionLabel la = fa.getFunctionSymbol();
			FunctionLabel lb = fb.getFunctionSymbol();
	
			// TODO Vector label
			if (fa.getArgs().size() == fb.getArgs().size() && la.equals(lb)) {
	
				List<Expr> exprs = new ArrayList<Expr>();
				for (int i = 0; i < fa.getArgs().size(); ++i) {
					Expr ea = fa.getArgs().get(i);
					Expr eb = fb.getArgs().get(i);
	
					exprs.add(new E_Equals(ea, eb));
				}
	
				result = ExprUtils.andifyBalanced(exprs);
			}
		}
	
		return result;
	}


	/**
	 * Merges arguments that are constants together
	 * Used for concat: concat("a", "b", "c") becomes concat("abc");
	 * 
	 * @param concat
	 * @return
	 */
	public static ExprList mergeConsecutiveConstants(Iterable<Expr> exprs) {
		String prev = null;
		ExprList newExprs = new ExprList();
	
		for (Expr expr : exprs) {
			if (expr.isConstant()) {
				prev = (prev == null ? "" : prev)
						+ expr.getConstant().asString();
			} else {
				if (prev != null) {
					newExprs.add(NodeValue.makeString(prev));
					prev = null;
				}
				newExprs.add(expr);
			}
		}
	
		if (prev != null) {
			newExprs.add(NodeValue.makeString(prev));
		}
	
		return newExprs;
	}


	public static boolean isConcatExpr(Expr expr) {
		return expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive; 
	}


	/**
	 * Optimizes Equals(Concat(argsA), Concat(argsB)) FIXME: An combinations,
	 * where there are constants - Equals(Concat(args), const)
	 * 
	 * //Assumes optimized form (the whole prefix in a single arg)
	 * 
	 * The following cases are being handled: concat(prefixA, restA) =
	 * concat(prefixB, restB) If none of the prefixes is a substring of the
	 * other, the whole expression evaluated to false. otherwise, if the
	 * prefixes are equal, they will be removed. if only one further argument
	 * remains, the concat will be removed.
	 * 
	 * Also, if one of the arguments is a constant, then it is treated as
	 * Concat(const), and theabove rules are applied
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr optimizeEqualsConcat(ExprFunction fn) {
		
		if(fn instanceof ExprFunction2) {
			ExprFunction2 tmp = (ExprFunction2)fn;
			
			Expr result = optimizeEqualsConcat(tmp);
			
			return result;
		}
		
		return fn;
	}
	
	
	public static Expr optimizeEqualsConcat(ExprFunction2 fn) {
		Expr ta = fn.getArg1();
		Expr tb = fn.getArg2();
	
		// None of the arguments is a concat-expression.
		if (!isConcatExpr(ta) && !isConcatExpr(tb)) {
			return fn;
		}
	
		// Create a list of concat-arguments (if not a concat, treat the expression
		// as an argument
		List<Expr> la = isConcatExpr(ta) ? ta.getFunction().getArgs()
				: Collections.singletonList(ta);
		List<Expr> lb = isConcatExpr(tb) ? tb.getFunction().getArgs()
				: Collections.singletonList(tb);
	
		// FIXME For efficiency, this could be done once in a separate step
		la = mergeConsecutiveConstants(la).getList();
		lb = mergeConsecutiveConstants(lb).getList();
	
		Expr result = SqlTranslationUtils.optimizeEqualsConcatAlign(la, lb);
	
		return result;
	}


	public static Expr optimizeEqualsConcatAlign(List<Expr> la, List<Expr> lb)
	{
		List<Alignment> cs = SqlExprOptimizer.align(la, lb);
		
		List<Expr> ors = new ArrayList<Expr>();
		for(Alignment c : cs) {
			
			List<Expr> ands = new ArrayList<Expr>();
	
			if(c.isSameSize()) {
			
				for(int i = 0; i < c.getKey().size(); ++i) {
					Expr ea = c.getKey().get(i);
					
					/*
					if(i >= c.getValue().size()) {
						System.out.println("OOpps");
					}
					*/
					
					Expr eb = c.getValue().get(i);
					
					if(ea.isConstant() && ea.equals(eb)) {
						continue;
					}
					
					E_Equals eq = new E_Equals(ea, eb);
					ands.add(eq);
				}
			} else {
				ands.add(
					new E_Equals(
						new E_StrConcatPermissive(new ExprList(c.getKey())),
						new E_StrConcatPermissive(new ExprList(c.getValue()))
					));
			}
			
			Expr and = ExprUtils.andifyBalanced(ands);
			ors.add(and);
		}
		
		if(ors.size() == 0) {
			return NodeValue.FALSE;
		}
		
		Expr result = ExprUtils.orifyBalanced(ors);
		
		return result;
	}

	
	/**
	 * The expression (a < b), where a and b are
	 * RdfTerm(type, obj, lang, datatype),
	 * could become:
	 * 
	 * 
	 * Worst case:
	 * 
	 * a.type < b.type ||
	 * a.type = b.type && a.obj < b.obj ||
	 * a.type = b.type && a.type = 2 && a.obj = b.obj && a.lang < b.lang ||
	 * a.type = b.type && a.type = 3 && a.obj = b.obj && a.datatype < b.datatype ||
	 * 
	 * Optimization: Skip datatype and language in comparisions.
	 * 
	 * 
	 * TODO We have to consider datatypes to rule out invalid
	 * comparisions (in regard to the datatypes).
	 * However, this should be done on the SQL level.
	 * 
	 */
	public static Expr translate(E_LessThan expr) {
		return translateCompare(expr);
	}

	public static Expr translate(E_LessThanOrEqual expr) {
		return translateCompare(expr);
	}
	
	public static Expr translate(E_GreaterThan expr) {
		return translateCompare(expr);
	}

	public static Expr translate(E_GreaterThanOrEqual expr) {
		return translateCompare(expr);
	}

	
	/*
	public static Expr optimize(E_LogicalAnd expr) {
		Expr a = optimizeMM(expr.getArg1());
		Expr b = optimizeMM(expr.getArg1());
		
		logger.debug("TODO Handle type error correctly");
		if(a.equals(NodeValue.FALSE) || b.equals(NodeValue.FALSE)) {
			return NodeValue.FALSE;
		}
		if(a.equals(NodeValue.TRUE) && b.equals(NodeValue.TRUE)) {
			return NodeValue.TRUE;
		} else {
			return new E_LogicalAnd(a, b);
		}
		
		
	}*/
	
	/*
	class Factory2Ctor<T>
		implements IFactory2<T>
	{
		private Class clazz;
		
		public Factory2Ctor(Class clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public T create(T a, T b) {
			Constructor<T> ctor = clazz.getConstructor(a, b);
			return ctor.newInstance(a, b);			
		}		
	}*/

	public static Expr translateCompare(ExprFunction2 expr) {
		return translateCompare(expr.getArg1(), expr.getArg2(), expr.getClass());
	}

	public static Expr translateCompare(Expr a, Expr b, final Class<?> clazz) {
		Factory2<Expr> factory = new Factory2<Expr>() {
			@Override
			public Expr create(final Expr a, final Expr b) {
				try {
					// FIXME We assume that jena's first constructor for
					// E_LessThan, E_LessThanOrEqual, etc. classes is
					// the one that takes two arguments of type Expr
					Constructor<?> ctor = clazz.getConstructors()[0]; //getConstructor(a.getClass(), b.getClass());
					return (Expr)ctor.newInstance(a, b);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}};
			
		return translateCompare(a, b, factory);
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * FIXME The following expression is crap. I must have written it before I was clear about type errors...
	 * a.type < b.type ||
	 * a.type = b.type && a.obj < b.obj ||
	 * a.type = b.type && a.type = 2 && a.obj = b.obj && a.lang < b.lang ||
	 * a.type = b.type && a.type = 3 && a.obj = b.obj && a.datatype < b.datatype ||
	 *
	 *
	 *
	 * @param a
	 * @param b
	 * @param exprFactory Factory for creating a comparision expression
	 * @return
	 */
	public static Expr translateCompare(Expr ea, Expr eb, Factory2<Expr> factory) {
				
		E_RdfTerm a = SqlPrePusher.asRdfTerm(ea);
		E_RdfTerm b = SqlPrePusher.asRdfTerm(eb);
		
		if(a == null || b == null) {
			//throw new RuntimeException("Arguments are no ExprRdfTerms");
			logger.warn("Arguments are no ExprRdfTerms");
			return factory.create(ea, eb);
		}
		
		NodeValue zero = NodeValue.makeInteger(0);
		NodeValue one = NodeValue.makeInteger(1);
		NodeValue two = NodeValue.makeInteger(2);
		NodeValue three = NodeValue.makeInteger(3);
		
		// This is the simple version that does not compare language and datatype
		/*
		Expr result =
			ExprUtils.orifyBalanced(
					factory.create(a.getType(), b.getType()),
					ExprUtils.andifyBalanced(new E_Equals(a.getType(), b.getType()), factory.create(a.getLexicalValue(), b.getLexicalValue()))
					);
		 */
		
		/*
		Expr result =
					ExprUtils.andifyBalanced(new E_Equals(a.getType(), b.getType()), factory.create(a.getLexicalValue(), b.getLexicalValue()));
		*/
		Expr result =
			ExprUtils.andifyBalanced(factory.create(a.getLexicalValue(), b.getLexicalValue()));
		
		return result;
	}

	public static ExprEvaluator createDefaultEvaluator() {
		ExprTransformerMap exprTransformer = new ExprTransformerMap();		
		ExprEvaluatorPartial evaluator = new ExprEvaluatorPartial(FunctionRegistry.get(), exprTransformer);
		
		Map<String, ExprTransformer> transMap = exprTransformer.getTransformerMap();
		
		transMap.put("concat", new ExprTransformerConcatNested());
		transMap.put("lang", new ExprTransformerLang());
		transMap.put("=", new ExprTransformerRdfTermComparator(evaluator));
		
		transMap.put("&&", new ExprTransformerLogicalAnd());

		return evaluator;
	}
}
