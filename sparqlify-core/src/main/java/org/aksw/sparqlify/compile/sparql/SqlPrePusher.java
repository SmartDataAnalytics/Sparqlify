package org.aksw.sparqlify.compile.sparql;


import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GenericSqlExpr;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GeomFromText;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_Intersects;
import org.aksw.sparqlify.trash.ExprCopy;

import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;

/**
 * Methods of this class are invoked for each rdf-function
 * after all its arguments have been pushed.
 * 
 * 
 * 
 * 
 * TODO Rename to PostPush. 
 * 
 * 
 * Documentation below is outdated.
 * -------------------------------------------------
 * Invoked on methods before a push is attempted.
 * This gives us a chance to tranform for instance
 * regex(term(id, lex, lang, dt)) into
 * regex(lex)
 * 
 * So basically it gives functions an opportunity to capture rdfterm arguments
 * 
 * By default arguments are pushed first.
 * 
 * TODO We need to identify different types of traversal
 * (probably just child first (bottom up) and parent first (top down)
 * and somehow allow to plug in the transformers
 * 
 * @author raven
 *
 */
public class SqlPrePusher
{
	public static Expr prePush(ExprFunction expr) {
		return (Expr)MultiMethod.invokeStatic(SqlPrePusher.class, "_prePush", expr);
	}
	
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
		}*/
		
		return result;
	}

	public static Expr _prePush(Expr expr) {
		return expr;
	}
	
	/*
	public static Expr _prePush(E_Lang expr) {
		ExprRdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));

		if(rdfTerm == null) {
			return expr;
		} else {
			//return ExprCopy.copyMM(expr, rdfTerm.getLanguageTag());
			return rdfTerm.getLanguageTag();
		}		
	}*/

	
	
	public static Expr getTypeOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getType() : expr;
		return result;
	}

	public static Expr getLexicalValueOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getLexicalValue() : expr;
		return result;
	}
	
	public static Expr getDatatypeOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getDatatype() : expr;
		return result;
	}

	public static Expr getLanguageTagOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getLanguageTag() : expr;
		return result;
	}


	/*
	public static Expr _prePush(E_Equals expr) {
		System.out.println("Here");
		
		
		return null;
	}*/
	
	
	/*
	public static Expr _prePush(E_IsIRI expr) {
		return new E_Equals(getTypeOrExpr(expr.getArg()), new ExprSqlBridge(new SqlExprValue(1)));
	}
	*/
	

	public static Expr _prePush(E_OneOf expr) {
		ExprList newArgs = new ExprList();
		for(Expr arg : expr.getArgs()) {
			newArgs.add(getLexicalValueOrExpr(arg));
		}
		return ExprCopy.getInstance().copy(expr, newArgs);
	}

	
	public static Expr _prePush(E_Intersects expr) {	
		return new E_Intersects(getLexicalValueOrExpr(expr.getArg1()), getLexicalValueOrExpr(expr.getArg2()));
	}
	
	public static Expr _prePush(E_GenericSqlExpr expr) {
		ExprList newArgs = new ExprList();
		for(Expr arg : expr.getArgs()) {
			newArgs.add(getLexicalValueOrExpr(arg));
		}
		return ExprCopy.getInstance().copy(expr, newArgs);
	}
	
	
	public static Expr _prePush(E_GeomFromText expr) {
		return new E_GeomFromText(getLexicalValueOrExpr(expr.getArg()));
	}
	
	//public static Expr _pre
	
	public static Expr _prePush(E_LangMatches expr) {
		return new E_LangMatches(getLanguageTagOrExpr(expr.getArg1()), getLexicalValueOrExpr(expr.getArg2()));
		/*
		ExprRdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));
		
		if(rdfTerm == null) {
			return expr;
		} else {
			return ExprCopy.copyMM(expr, rdfTerm.getLanguageTag());
		}*/		
	}

	public static Expr _prePush(E_Str expr) {
		//return asRdfTerm(expr.getArg()).getLexicalValue();
		return getLexicalValueOrExpr(expr.getArg());
		//ExprRdfTerm result =  new ExprRdfTerm(NodeValue.makeInteger(2), getLexicalValueOrExpr(expr.getArg()), NodeValue.makeString(""), NodeValue.makeString(""));
		//return result;
	}
	
	public static Expr _prePush(E_Regex expr) {
		E_RdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));
			
		if(rdfTerm == null) {
			return expr;
		} else {
			ExprList args = new ExprList();
			args.add(rdfTerm.getLexicalValue());
			for(int i = 1; i < expr.getArgs().size(); ++i) {
				args.add(expr.getArgs().get(i));
			}
			
			return ExprCopy.getInstance().copy(expr, args);
		}
	}
	
	
	/*
	public static Aggregator _prePush(AggCount count) {
		E_RdfTerm rdfTerm = asRdfTerm
	}*/
	
	public static Expr _prePush(ExprFunction expr) {
		return getLexicalValueOrExpr(expr);
	}
}