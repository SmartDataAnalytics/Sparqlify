package org.aksw.sparqlify.core.query_plan;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * Time the execution of a sub plan (TODO: Maybe this should be hard wired into a base class in the first place)
 * 
 */
public class QEP_Time
	extends QEP_Base1
{
	private Model target; // The model into which to write the timing stats

	public QEP_Time(Model target, QueryExecutionPlanNode subPlan) {
		super(subPlan);
		this.target = target;
	}

	@Override
	public ResultSet execute() {
		long start = System.nanoTime();
		
		// TODO: Wrap the result set and take the time it takes to iterate it
		ResultSet result = super.execute();
		long end = System.nanoTime();
		long elapsed = end - start;
		
		return result;
	}
	
	
}
