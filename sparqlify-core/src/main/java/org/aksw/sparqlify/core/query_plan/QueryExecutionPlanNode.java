package org.aksw.sparqlify.core.query_plan;

import com.hp.hpl.jena.query.ResultSet;

public interface QueryExecutionPlanNode {
	ResultSet execute();
}
