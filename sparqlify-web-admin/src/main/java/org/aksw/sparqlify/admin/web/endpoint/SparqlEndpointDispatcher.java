package org.aksw.sparqlify.admin.web.endpoint;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.web.SparqlEndpointBase;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;


@Service
//@Path("/{path : ([^/]+)?}/sparql/")
@Path("/{path}/sparql/")
public class SparqlEndpointDispatcher
	extends SparqlEndpointBase
{
//	@Resource(name="sparqlServiceConfig")
//	private SparqlServiceManager sparqlServiceConfig;

	//private Map<String, ServiceControl<QueryExecutionFactory>> nameToService;
	
	@Resource(name="sparqlServiceMap")
	private Map<String, QueryExecutionFactory> nameToService;
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private ServletContext servletContext;
	
	@Context
	private HttpServletResponse response;
	
	
	public QueryExecutionFactory requireService() {
		MultivaluedMap<String, String> params = uriInfo.getPathParameters();
		String path = params.getFirst("path");		
		
		QueryExecutionFactory result = nameToService.get(path); //sparqlServiceConfig.getServiceMap().get(path);
		if(result == null) {
			throw new RuntimeException("No service registered for " + path);
		}

		return result;
	}
	
	@Override
	public QueryExecution createQueryExecution(Query query,
			HttpServletRequest req) {

		QueryExecutionFactory qef = requireService();
		
		QueryExecution result = qef.createQueryExecution(query);
		
		return result;
	}
	
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response executeQueryXml(@Context HttpServletRequest req) //, @Context HttpServletResponse res)
			throws Exception {
		
		requireService();
		
		InputStream r = servletContext.getResourceAsStream("/resources/snorql/index.html");
		System.out.println("Resource is " + r);
		return Response.ok(r, MediaType.TEXT_HTML).build();
	}

}

