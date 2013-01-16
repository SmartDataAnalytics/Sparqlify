package org.aksw.sparqlify.core.datatypes;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.ExprSubstitutorSql;


public class SparqlFunctionImpl
	implements SparqlFunction
{
	private String name;
	private SqlExprEvaluator evaluator;
	private ExprSubstitutorSql substitutor;
	private MethodSignature<TypeToken> signature;
	
	public SparqlFunctionImpl(String name, SqlExprEvaluator evaluator) {
		this(name, evaluator, null);
	}

//	public SparqlFunctionImpl(String name, SqlExprEvaluator evaluator, ExprSubstitutorSql substitutor) {
//		this(name, null, evaluator, substitutor);
//	}

	public SparqlFunctionImpl(String name, SqlExprEvaluator evaluator, ExprSubstitutorSql substitutor) {
		this(name, null, evaluator, substitutor);
	}

	public SparqlFunctionImpl(String name, MethodSignature<TypeToken> signature, SqlExprEvaluator evaluator, ExprSubstitutorSql substitutor) {
		this.name = name;
		this.signature = signature;
		this.evaluator = evaluator;
		this.substitutor = substitutor;
	}

	public String getName() {
		return name;
	}

//	public void setName(String name) {
//		this.name = name;
//	}

	public SqlExprEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(SqlExprEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public ExprSubstitutorSql getSubstitutor() {
		return substitutor;
	}

	public void setSubstitutor(ExprSubstitutorSql substitutor) {
		this.substitutor = substitutor;
	}

	@Override
	@Deprecated
	public Collection<XMethod> getSqlMethods() {
		throw new RuntimeException("Should not be called");
	}

	@Override
	@Deprecated
	public SqlMethodCandidate lookup(List<TypeToken> argTypes) {
		throw new RuntimeException("Should not be called");
	}

	@Override
	public MethodSignature<TypeToken> getSignature() {
		throw new RuntimeException("Not implemented");
	}
}
