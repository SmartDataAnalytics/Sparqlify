package org.aksw.sparqlify.admin.web.api;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
import org.aksw.jenax.web.servlet.SparqlEndpointBase;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.update.UpdateProcessor;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;


@Service
@Path("/api/sparql/")
public class SparqlifyManagerSparqlEndpoint
    extends SparqlEndpointBase
{

    @Resource(name="managerApiQef")
    private QueryExecutionFactory qef;

    @Context
    private ServletContext servletContext;


//    @Override
//    public QueryExecution createQueryExecution(Query query) {
//        QueryExecution result = qef.createQueryExecution(query);
//        return result;
//    }
//
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

    @Override
    protected RDFConnection getConnection() {
        return new RDFConnectionModular(new SparqlQueryConnectionJsaBase<>(qef), null, null);
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
