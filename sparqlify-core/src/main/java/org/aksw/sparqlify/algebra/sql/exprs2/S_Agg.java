package org.aksw.sparqlify.algebra.sql.exprs2;

import org.openjena.atlas.io.IndentedWriter;

/**
 * Class for holding aggregator functions
 * 
 * @author raven
 *
 */
public class S_Agg
	extends SqlExpr1
{
	private SqlAggregator aggregator;

	public S_Agg(SqlAggregator aggregator) {
		super(aggregator.getDatatype(), aggregator.getClass().getName(), aggregator.getExpr());
		this.aggregator = aggregator;
	}

	public SqlAggregator getAggregator() {
		return aggregator;
	}

	@Override
	public void asString(IndentedWriter writer) {
		aggregator.asString(writer);
		//writer.print("Aggregator");
		//writeArgs(writer);
	}

	@Override
	public S_Agg copy(SqlExpr arg) {
		//aggregator.copy()
		throw new RuntimeException("Not implemented");
	}

}
