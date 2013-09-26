package org.aksw.sparqlify.admin.web.api;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.web.SparqlEndpointBase;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


@Path("/api/sparql")
@Service
public class SparqlifyManagerSparqlEndpoint
	extends SparqlEndpointBase
{

	@Resource(name="managerApiQef")
	private QueryExecutionFactory qef;
	
	@Override
	public QueryExecution createQueryExecution(Query query,
			HttpServletRequest req) {
		
		QueryExecution result = qef.createQueryExecution(query);
		return result;
	}
}
