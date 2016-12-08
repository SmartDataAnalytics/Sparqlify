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
import org.aksw.jena_sparql_api.core.utils.QueryExecutionAndType;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUpdate;
import org.aksw.jena_sparql_api.web.servlets.SparqlEndpointBase;
import org.aksw.service_framework.core.SparqlService;
import org.aksw.sparqlify.core.sparql.QueryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryFactoryEx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateProcessor;
import org.springframework.stereotype.Service;


@Service
@Path("/{path}/sparql/")
//@Path("/{path : ([^/]+)?}/sparql/")
public class SparqlEndpointDispatcher
    extends SparqlEndpointBase
{
    @Resource(name="sparqlServiceMap")
    //private Map<String, QueryExecutionFactory> nameToService;
    private Map<String, SparqlService> nameToService;

    @Context
    private UriInfo uriInfo;

    @Context
    private ServletContext servletContext;

    @Context
    private HttpServletResponse response;


    public QueryExecutionFactory requireService() {
        MultivaluedMap<String, String> params = uriInfo.getPathParameters();
        String path = params.getFirst("path");

        SparqlService service = nameToService.get(path);
        //QueryExecutionFactory result = nameToService.get(path); //sparqlServiceConfig.getServiceMap().get(path);
        if(service == null) {
            throw new RuntimeException("No service registered for " + path);
        }

        QueryExecutionFactory result = service.getSparqlService();

        return result;
    }

    @Override
    public QueryExecutionAndType createQueryExecutionAndType(String queryString) {

        QueryExecutionFactory qef = requireService();


        QueryExecutionAndType result;
        QueryExecution qe;
        if(qef instanceof QueryExecutionFactoryEx) {
            QueryEx queryEx = QueryFactoryEx.create(queryString);
            QueryExecutionFactoryEx tmp = (QueryExecutionFactoryEx)qef;

            qe = tmp.createQueryExecution(queryEx);
            result = new QueryExecutionAndType(qe, queryEx.getQuery().getQueryType());
        } else {

            Query query = QueryFactory.create(queryString);
            qe = qef.createQueryExecution(query);
            result = new QueryExecutionAndType(qe, query.getQueryType());
        }
        //QueryExecutionFactoryExImpl


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

    @Override
    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) {
        // TODO Auto-generated method stub
        return null;
    }
}

