package org.aksw.sparqlify.inverse;

import java.util.List;

import org.apache.jena.sparql.core.Quad;

/**
 * Map a quad back to a set to which views could have generated it
 * with which values in the underlying table
 * 
 * 
 * @author raven
 *
 */
public interface SparqlSqlInverseMapper
{
	public List<SparqlSqlInverseMap> map(Quad quad);
}