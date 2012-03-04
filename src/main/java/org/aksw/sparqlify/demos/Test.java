package org.aksw.sparqlify.demos;

import java.io.UnsupportedEncodingException;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class Test {
	public static void main(String[] args) throws UnsupportedEncodingException {
		testSparqlEndPointService();
	}

	public static void testSparqlEndPointService() {

		String service = "http://linkedgeodata.org/sparql";

		String queryString = "select ?id bif:st_distance(?geo,bif:st_point       (55.46667098999023,8.449999809265137)) as ?distance "
				+ "From <http://linkedgeodata.org> "
				+ "where { "
				+ "?id geo:geometry ?geo. "
				+ "filter (bif:st_intersects(?geo,<bif:st_point>(55.46667098999023,8.449999809265137),350.0))."
				+ "} order by ?distance";

		QueryExecution vqe = new QueryEngineHTTP(service, queryString);
		
		ResultSet results = vqe.execSelect();
		while (results.hasNext()) {
			System.out.println(results.next());
		}
	}

}

//QueryExecution vqe = QueryExecutionFactory.sparqlService(service, queryString);
