package org.aksw.sparqlify.admin.web.api;

import java.io.InputStream;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.web.SparqlEndpointBase;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


@Service
@Path("/api/sparql/")
public class SparqlifyManagerSparqlEndpoint
	extends SparqlEndpointBase
{

	@Resource(name="managerApiQef")
	private QueryExecutionFactory qef;
	
	@Context
	private ServletContext servletContext;

	
	@Override
	public QueryExecution createQueryExecution(Query query,
			HttpServletRequest req) {
		
		QueryExecution result = qef.createQueryExecution(query);
		return result;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response executeQueryXml(@Context HttpServletRequest req) //, @Context HttpServletResponse res)
			throws Exception {
				
		InputStream r = servletContext.getResourceAsStream("/resources/snorql/index.html");
		System.out.println("Resource is " + r);
		return Response.ok(r, MediaType.TEXT_HTML).build();
	}

}
