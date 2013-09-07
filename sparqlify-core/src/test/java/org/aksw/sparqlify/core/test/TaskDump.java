package org.aksw.sparqlify.core.test;

import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.util.SparqlifyUtils;

import com.hp.hpl.jena.sparql.core.Quad;

public class TaskDump
	implements Callable<Set<Quad>>
{
	private QueryExecutionFactory qef;

	public TaskDump(QueryExecutionFactory qef)
	{
		this.qef = qef;
	}

	@Override
	public Set<Quad> call() throws Exception {
		Set<Quad> result = SparqlifyUtils.createDumpNQuads(qef);
		return result;
	}
	
}