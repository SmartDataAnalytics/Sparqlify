package org.aksw.sparqlify.core.query_plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ResultSet;

/**
 * Combine the result sets of sub-plans
 * 
 * @author raven
 *
 */
public class QEP_Union
	implements QueryExecutionPlanNode
{
	private List<QueryExecutionPlanNode> subPlans = new ArrayList<QueryExecutionPlanNode>();

	@Override
	public ResultSet execute() {
		//List<ResultSet> resultSets = new ArrayList<ResultSets>();s

		
		// TODO Auto-generated method stub
		return null;
	}

}
