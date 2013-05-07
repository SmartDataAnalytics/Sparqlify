package org.aksw.sparqlify.core.algorithms;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;

public class SqlOptimizer {
	public static void optimize(SqlOp op) {
		if(op instanceof SqlOpSelectBlock) {
			SqlOpSelectBlock block = (SqlOpSelectBlock)op;
			
			List<Collection<SqlExpr>> cnf = SqlExprUtils.toCnf(block.getConditions());
		
			SqlExprUtils.optimizeNotNullInPlace(cnf);
		}
		
		List<SqlOp> subOps = op.getSubOps();
		for(SqlOp subOp : subOps) {
			optimize(subOp);
		}
		
	}
}