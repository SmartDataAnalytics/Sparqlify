package org.aksw.sparqlify.core.query_plan;

import com.hp.hpl.jena.query.ResultSet;

public abstract class QEP_Base1
	implements QueryExecutionPlanNode
{
	private QueryExecutionPlanNode subPlan;
	
	public QEP_Base1(QueryExecutionPlanNode subPlan) {
		this.subPlan = subPlan;
	}
	
	public QueryExecutionPlanNode getSubPlan() {
		return subPlan;
	}

	@Override
	public ResultSet execute() {
		return subPlan.execute();
	}
}
