package org.aksw.sparqlify.core;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.engine.binding.Binding;

public class ResultSetSparqlify
	implements org.apache.jena.query.ResultSet
{
	int rowNumber = 0;
	private List<String> resultVars;
	private Iterator<Binding> it;

	public ResultSetSparqlify(Iterator<Binding> it, List<String> resultVars, int rowNumber)
	{
		this.it = it;
		this.resultVars = resultVars;
		this.rowNumber = rowNumber;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public QuerySolution next() {
		return new ResultBinding(null, nextBinding()) ;
	}

	@Override
	public QuerySolution nextSolution() {
		return next();
	}

	@Override
	public Binding nextBinding() {
		return it.next();
	}

	@Override
	public int getRowNumber() {
		return rowNumber;
	}

	@Override
	public List<String> getResultVars() {
		return resultVars;
	}

	@Override
	public Model getResourceModel() {
		return null;
	}
}