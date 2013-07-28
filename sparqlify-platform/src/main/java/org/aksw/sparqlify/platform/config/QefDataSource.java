package org.aksw.sparqlify.platform.config;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

import de.fuberlin.wiwiss.pubby.DataSource;

/**
 * A data source backed by a QueryExecutionFactory.
 * 
 * @author Claus Stadler
 */
public class QefDataSource implements DataSource {
	private String previousDescribeQuery;
	private QueryExecutionFactory qef;

	private String endpointURN;
	/*
	private String defaultGraphName;
	*/

	public QefDataSource(QueryExecutionFactory qef, String endpointURN) {
		this.qef = qef;
		this.endpointURN = endpointURN;
	}

	public String getEndpointURL() {
		return endpointURN; 
	}

	public String getResourceDescriptionURL(String resourceURI) {
			return "DESCRIBE <" + resourceURI + ">";
			/*
		try {
			StringBuffer result = new StringBuffer();
			result.append(endpointURL);
			result.append("?");
			if (defaultGraphName != null) {
				result.append("default-graph-uri=");
				result.append(URLEncoder.encode(defaultGraphName, "utf-8"));
				result.append("&");
			}
			result.append("query=");
			result.append(URLEncoder.encode("DESCRIBE <" + resourceURI + ">",
					"utf-8"));
			return result.toString();
		} catch (UnsupportedEncodingException ex) {
			// can't happen, utf-8 is always supported
			throw new RuntimeException(ex);
		}
			*/
	}

	public Model getResourceDescription(String resourceURI) {
		return execDescribeQuery("DESCRIBE <" + resourceURI + ">");
	}

	public Model getAnonymousPropertyValues(String resourceURI,
			Property property, boolean isInverse) {
		String query = "DESCRIBE ?x WHERE { "
				+ (isInverse ? "?x <" + property.getURI() + "> <" + resourceURI
						+ "> . " : "<" + resourceURI + "> <"
						+ property.getURI() + "> ?x . ")
				+ "FILTER (isBlank(?x)) }";
		
		Model result = execDescribeQuery(query);
		return result;
	}


	public String getPreviousDescribeQuery() {
		return previousDescribeQuery;
	}

	private Model execDescribeQuery(String query) {
		previousDescribeQuery = query;
		QueryExecution qe = qef.createQueryExecution(query);
		
		Model result = qe.execDescribe();

		/*
		if (defaultGraphName != null) {
			endpoint.setDefaultGraphURIs(Collections
					.singletonList(defaultGraphName));
		}
		*/
		return result;
	}
}
