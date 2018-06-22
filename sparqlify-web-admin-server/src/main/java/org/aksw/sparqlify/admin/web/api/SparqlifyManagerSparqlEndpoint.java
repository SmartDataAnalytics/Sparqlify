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
import org.aksw.jena_sparql_api.stmt.SparqlStmtUpdate;
import org.aksw.jena_sparql_api.web.servlets.SparqlEndpointBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.update.UpdateProcessor;
import org.springframework.stereotype.Service;


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
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = qef.createQueryExecution(query);
        return result;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) {
        return null;
    }

    @GET
	@Produces({ MediaType.APPLICATION_JSON, "application/sparql-results+json" })
    @Path("namespaces.js")
    public String namespaces() {
    	String jsonMap = "{}";
    	String result = "var D2R_namespacePrefixes = " + jsonMap;

    	return result;
    }

//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public Response executeQueryXml(@Context HttpServletRequest req) //, @Context HttpServletResponse res)
//            throws Exception {
//
//        InputStream r = servletContext.getResourceAsStream("/resources/snorql/index.html");
//        //System.out.println("Resource is " + r);
//        return Response.ok(r, MediaType.TEXT_HTML).build();
//    }

}
