package org.aksw.sparqlify.core.datatypes;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.core.TypeToken;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class SparqlFunctionImpl
	implements SparqlFunction
{
	private String name;
	private SqlExprEvaluator evaluator;
	//private DatatypeSystem datatypeSystem;
	
	private Multimap<String, XMethod> nameToSqlFunction = HashMultimap.create();
	
	public SparqlFunctionImpl(String name, SqlExprEvaluator evaluator) {
		//this.datatypeSystem = datatypeSystem;
		this.name = name;
		this.evaluator = evaluator;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public SqlExprEvaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * Use the result of this method to modify the mappings
	 * 
	 * @return
	 */
	public Multimap<String, XMethod> getSqlFunctionMap() {
		return nameToSqlFunction;
	}
	
	@Override
	public Collection<XMethod> getSqlMethods() {
		Collection<XMethod> result = nameToSqlFunction.values();
		return result;
	}


	@Override
	public SqlMethodCandidate lookup(List<TypeToken> argTypes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void registerSqlFunction(XMethod sqlFunction) {
		nameToSqlFunction.put(sqlFunction.getName(), sqlFunction);
	}
}
