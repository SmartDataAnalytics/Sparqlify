package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.interfaces.TranslatorSql;

import com.hp.hpl.jena.sparql.expr.Expr;






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
	public Expr translateSql(Expr expr) {

		Expr result = evaluator.eval(expr, null);
		
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
