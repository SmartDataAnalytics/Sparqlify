package org.aksw.sparqlify.core.cast;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator;


public class CoercionSparqlFunction
	//implements ExprTransformer
{
	private TypeSystem typeSystem;
	private String functionURI;
	
	public CoercionSparqlFunction(TypeSystem typeSystem, String functionURI) {
		this.typeSystem = typeSystem;
		this.functionURI = functionURI;
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public String getFunctionURI() {
		return functionURI;
	}

	//@Override
	public SqlExpr transform(SqlExpr expr) throws CastException {
		
		List<SqlExpr> arg = Collections.singletonList(expr);

		SparqlFunction function = typeSystem.getSparqlFunction(functionURI);
		SqlExprEvaluator evaluator = function.getEvaluator();
				
		SqlExpr result = null;
		if(evaluator != null) {
			result = evaluator.eval(arg);
		}
		
		if(result != null) {
			return result;
		}
		
		ExprSubstitutor substitutor = function.getSubstitutor();
		
		if(substitutor != null) {
			result = substitutor.create(arg);
		}
		
		return result;
	}
	
	
}
