package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;

import com.hp.hpl.jena.sdb.core.JoinType;

public class SqlOptimizer {

	
	
	public static void optimize(SqlOp op) {
		if(op instanceof SqlOpSelectBlock) {
			SqlOpSelectBlock block = (SqlOpSelectBlock)op;
			
			List<Collection<SqlExpr>> cnf = SqlExprUtils.toCnf(block.getConditions());
		
			SqlExprUtils.optimizeNotNullInPlace(cnf);
			
			SqlOp subOp = block.getSubOp();
			List<SqlOp> subOps = collectJoins(subOp);
			
			System.out.println("Joins collected: " + subOps.size());
			
			if(subOps.size() > 1) {
				for(SqlOp s : subOps) {
					//s.getSchema().
					System.out.println("     Member schema: " + s.getSchema());
				}
			}
			
		}
		
		List<SqlOp> subOps = op.getSubOps();
		for(SqlOp subOp : subOps) {
			optimize(subOp);
		}
		
	}
	
	public static List<SqlOp> collectJoins(SqlOp sqlOp) {
		List<SqlOp> result = new ArrayList<SqlOp>();
		
		collectJoins(sqlOp, result);
		
		return result;
	}
	
	public static void collectJoins(SqlOp sqlOp, List<SqlOp> result) {
		if(sqlOp instanceof SqlOpJoin) {
			
			SqlOpJoin opJoin = ((SqlOpJoin)sqlOp);
			
			if(opJoin.getJoinType().equals(JoinType.INNER)) {
			
				SqlOp a = opJoin.getLeft();
				SqlOp b = opJoin.getRight();
	
				collectJoins(a, result);
				collectJoins(b, result);
				
				return;
			}
		} 
		
		result.add(sqlOp);
	}
}