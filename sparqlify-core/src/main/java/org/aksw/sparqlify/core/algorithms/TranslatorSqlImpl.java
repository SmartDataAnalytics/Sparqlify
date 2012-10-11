package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mapping.SparqlifyConstants;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GenericSqlExpr;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GeographyFromText;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GeomFromText;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_Intersects;
import org.aksw.sparqlify.algebra.sql.exprs.S_Add;
import org.aksw.sparqlify.algebra.sql.exprs.S_Cast;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeographyFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeometryFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_Intersects;
import org.aksw.sparqlify.algebra.sql.exprs.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.S_Substract;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.interfaces.TranslatorSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.E_StrDatatype;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;






/**
 * 
 * Translation of functions:
 * 
 * If all arguments could be converted to sql 
 * In general, this rule applies: 
 * 
 * tl(f_rdf(a1, ... an)) -> f_sql(tl(a1), ..., tl(an))
 *
 * Note: even if all arguments of a function are constants, id does not mean the function can be evaluated;
 * the function may be only available in the database.
 *
 * e = intersects(termCtor(geoCol), geomFromText("Some string"))
 * 
 * tl(e) -> ST_intersects(tl(termCtor(geoCol)), tl(geomFromText("Some string")))
 * termCtor(geoCol) -> tl(geoCol.value) 
 * tl(geomFromText("Some string") -> ST_geomFromText(tl("some String")) -> ST_geomFromText("some String")
 * 
 * 
 * tl(lang(rdfTerm(v, l)) -> l
 * 
 * 
 * (Partial) evaluation:
 * eval(fn(a1, ..., an)) ->
 * fn(eval(a1), ..., (an))
 * 
 * If any of the arguments 
 * 
 * 
 * tl(op_rdf(termctor, termctor)) //
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class TranslatorSqlImpl
	implements TranslatorSql
{
	private ExprEvaluator evaluator;
	
	public TranslatorSqlImpl() {
		this.evaluator = SqlTranslationUtils.createDefaultEvaluator();
	}
	
	@Override
	public Expr translateSql(Expr expr, Map<Var, Expr> binding) {

		Expr result = evaluator.eval(expr, binding);
		
		/*
		Expr pushed = PushDown.pushDownMM(tmp);
		if(!(pushed instanceof ExprSqlBridge)) {
			throw new RuntimeException("Failed to push down '" + expr + "'");
		}
		SqlExpr result = ((ExprSqlBridge)pushed).getSqlExpr();
		*/
		
		return result;
	}
}



