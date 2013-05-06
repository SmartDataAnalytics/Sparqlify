package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Collections;
import java.util.List;

import org.openjena.atlas.io.IndentedWriter;

/**
 * Class for holding aggregator functions
 * 
 * @author raven
 *
 */
public class S_Agg
	extends SqlExprN
{
	private SqlAggregator aggregator;

	public static List<SqlExpr> exprToList(SqlExpr expr) {
		List<SqlExpr> result;
		
		if(expr == null) {
			result = Collections.emptyList();
		} else {
			result = Collections.singletonList(expr);
		}
		
		return result;
	}
	
	public S_Agg(SqlAggregator aggregator) {
		super(aggregator.getDatatype(), aggregator.getClass().getName(), exprToList(aggregator.getExpr()));
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
	public S_Agg copy(List<SqlExpr> args) {
		assert args.size() <= 1 : "Only at most 1 argument expected, got: " + args;
		
		SqlExpr arg = null;
		if(!args.isEmpty()) {
			arg = args.get(0);
		}
		
		S_Agg result = copy(arg);
		return result;
	}


	public S_Agg copy(SqlExpr arg) {
		SqlAggregator newAgg = aggregator.copy(arg);
		
		S_Agg result = new S_Agg(newAgg);
		
		return result;
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
