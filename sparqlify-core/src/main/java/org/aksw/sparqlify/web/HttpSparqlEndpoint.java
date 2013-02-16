package org.aksw.sparqlify.web;


import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.sparql.engine.http.HttpParams;


/**
 * Jersey resource for exposing a SparqlEndpoint based on a QueryExecutionFactory
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
@Component
@Path("/sparql")
public class HttpSparqlEndpoint {

	//@Context
	//@Resource(mappedName="java:/sparqlifyDS")
	//@Context
	//@Autowired
	@Resource
	private QueryExecutionFactory queryExecutionFactory;
	//private DataSource dataSource;
	
	
	public void setQueryExecutionFactory(QueryExecutionFactory queryExecutionFactory) {
		this.queryExecutionFactory = queryExecutionFactory;
	}
	
	public QueryExecutionFactory getQueryExecutionFactorys() {
		return queryExecutionFactory;
	}
	
	//@Context
	//protected QueryExecutionFactory<QueryExecutionStreaming> sparqler = null;
	public static QueryExecutionFactory sparqler = null;
	

	// No-arg constructor
	// used for injecting the QueryExecutionFactory via Spring
	public HttpSparqlEndpoint() {
		
	}

	public HttpSparqlEndpoint(@Context ServletContext context) {
		QueryExecutionFactory qef = (QueryExecutionFactory)context.getAttribute("queryExecutionFactory");
		
		this.queryExecutionFactory = qef;
		
		init();
	}
	
	
	public void init() {
	}
	
	
	public QueryExecutionFactory getSparqler() throws Exception {
		if(sparqler == null) {
			sparqler = queryExecutionFactory;
		}
		
		if(sparqler == null) {
			throw new NullPointerException("The query execution factory has not been set.");
		}
		
		/*
		Connection conn = dataSource.getConnection();
		 
		RdfViewSystemOld.loadDatatypes(conn, system.getViews());
		conn.close();

		QueryExecutionFactory<QueryExecutionStreaming> qef = new QueryExecutionFactorySparqlifyDs(system, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetSize != null) {
			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetSize);
		}
		*/
		//System.out.println("My datasource context is " + dataSource);

		
		return sparqler;
	}

	public StreamingOutput processQuery(String queryString, String format)
			throws Exception
	{
		return ProcessQuery.processQuery(queryString, format, getSparqler());
	}

	/*
	@GET
	public String executeQueryXml()
			throws Exception {
		String example = "<?xml version='1.0' encoding='ISO-8859-1'?><xml>Select * { ?s ?p ?o } Limit 10</xml>";
		return "No query specified. Example: ?query=" + StringUtils.urlEncode(example);
	}
	*/

	/*
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String executeQuery()
			throws Exception {
		return "No query specified";
	}
	*/
	

	/*
	@GET
	public StreamingOutput executeQueryXml(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}
	*/

	
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput executeQueryXml(@QueryParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	//@Produces(MediaType.APPLICATION_XML)
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public StreamingOutput executeQueryXmlPost(@FormParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJson(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Json);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJsonPost(@FormParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Json);
	}
	
	//@Produces("application/rdf+xml")
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@GET
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXml(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}	
	

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXmlPost(@FormParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}

	@GET
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXml(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXmlPost(@FormParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryText(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Text);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryTextPost(@FormParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Text);
	}

	
	
	/*
	private String _corsHeaders;

	private Response makeCORS(ResponseBuilder req, String returnMethod) {
	   Response rb = req.ok()
	      .header("Access-Control-Allow-Origin", "*")
	      .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

	   if (!"".equals(returnMethod)) {
	      rb.header("Access-Control-Allow-Headers", returnMethod);
	   }

	   return rb.build();
	}

	private Response makeCORS(ResponseBuilder req) {
	   return makeCORS(req, _corsHeaders);
	}
	*/
	
	
	/*
	@GET
	public Response executeQueryXml(@Context HttpContext hc, @QueryParam("query") String queryString, @QueryParam("format") String format)
			throws Exception {

		hc.getResponse().getHttpHeaders().put("Content-Type", SparqlFormatterUtils.FORMAT_Json);
		
		if(queryString == null) {
			hc.getResponse().getOutputStream()
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		if(format != null) {
			return processQuery(queryString, SparqlFormatterUtils.FORMAT_Json);	
		}
		
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}
	*/
	
}