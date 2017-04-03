package org.aksw.sparqlify.core.query_plan;

import org.apache.jena.query.ResultSet;

public interface QueryExecutionPlanNode {
	ResultSet execute();
}
