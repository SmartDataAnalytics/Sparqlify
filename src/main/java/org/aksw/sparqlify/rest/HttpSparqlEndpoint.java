package org.aksw.sparqlify.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.sparql.engine.http.HttpParams;


/**
 * Jersey resource for exposing a SparqlEndpoint based on a QueryExecutionFactory
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
@Path("/sparql")
@Produces("application/rdf+xml")
public class HttpSparqlEndpoint {

	//@Context
	//protected QueryExecutionFactory<QueryExecutionStreaming> sparqler = null;
	public static QueryExecutionFactory<QueryExecutionStreaming> sparqler = null;
	
	public HttpSparqlEndpoint() {
		init();
	}
	
	
	public void init() {
	}
	
	
	public QueryExecutionFactory<QueryExecutionStreaming> getSparqler() throws Exception {
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

	@GET
	public StreamingOutput executeQueryXml(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput executeQueryJson(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Json);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput executeQueryJsonPost(@FormParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Json);
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
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryText(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_Text);
	}

}