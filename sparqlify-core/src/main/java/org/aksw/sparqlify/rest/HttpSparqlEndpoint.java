package org.aksw.sparqlify.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.sparql.engine.http.HttpParams;


class StreamingOutputString 
	implements StreamingOutput {

	String str;
	
	public StreamingOutputString(String str) {
		this.str = str;
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		PrintStream out = new PrintStream(output);
		
		out.println(str);
		out.flush();
	}
	
	public static StreamingOutputString create(String str) {
		return new StreamingOutputString(str);
	}
	
}

/**
 * Jersey resource for exposing a SparqlEndpoint based on a QueryExecutionFactory
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
@Component
@Path("/sparql")
@Produces("application/rdf+xml")
public class HttpSparqlEndpoint {

	//@Context
	//@Resource(mappedName="java:/sparqlifyDS")
	//@Context
	//@Autowired
	@Resource
	private DataSource dataSource;
	
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	//@Context
	//protected QueryExecutionFactory<QueryExecutionStreaming> sparqler = null;
	public static QueryExecutionFactory<QueryExecutionStreaming> sparqler = null;
	
	public HttpSparqlEndpoint() {
		init();
	}
	
	
	public void init() {
	}
	
	
	public QueryExecutionFactory<QueryExecutionStreaming> getSparqler() throws Exception {
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

	
	@GET
	//@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput executeQueryXml(@QueryParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}

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