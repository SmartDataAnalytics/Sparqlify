package org.aksw.sparqlify.views.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.database.GetVarsMentioned;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.google.common.collect.Sets;

import sparql.ClauseUtils;
import sparql.CnfUtils;

/**
 * @author raven
 *
 * TODO Each Sparql-algebra node should carry the applicable filters
 * (So: First copy the Op structure to custom Op classs, then rewrite the Filters)
 *
 */
public class FilterPlacementOptimizer {
	public static Op optimize(Op op) {
		Set<Set<Expr>> cnf = new HashSet<Set<Expr>>();
		return (Op)MultiMethod.invokeStatic(FilterPlacementOptimizer.class, "_optimize", op, cnf);
	}

	
	public static Op optimize(Op op, Set<Set<Expr>> cnf) {
		if(op instanceof OpNull) {
			return op;
		}
		
		
		return (Op)MultiMethod.invokeStatic(FilterPlacementOptimizer.class, "_optimize", op, cnf);
	}
	
	
	public static Set<Set<Expr>> filterByVars(Set<Set<Expr>> cnf, Op op) {
	
		return ClauseUtils.filterByVars(cnf, GetVarsMentioned.getVarsMentioned(op));

	}

	public static Op _optimize(OpOrder op, Set<Set<Expr>> cnf) {
		return new OpOrder(optimize(op.getSubOp(), cnf), op.getConditions());
	}
	
	
	public static Op _optimize(OpJoin op, Set<Set<Expr>> cnf) {
		
		Set<Set<Expr>> leftCnf = filterByVars(cnf, op.getLeft());
		Set<Set<Expr>> rightCnf = filterByVars(cnf, op.getLeft());
		
		Set<Set<Expr>> union = Sets.union(leftCnf, rightCnf);
		Set<Set<Expr>> remaining = Sets.difference(cnf, union);

		Op result = OpJoin.create(optimize(op.getLeft(), leftCnf), optimize(op.getRight(), rightCnf));
		
		if(!remaining.isEmpty()) {
			result = OpFilter.filter(cnfToExprList(remaining), result);
		}
		
		return result;
	}
	
	
	public static Op _optimize(OpDisjunction op, Set<Set<Expr>> cnf)
	{
		List<Op> args = new ArrayList<Op>();
		for(Op element : op.getElements()) {
			Set<Var> elementVars = GetVarsMentioned.getVarsMentioned(element);
			
			boolean elementHasRequiredVars = true;
			for(Set<Expr> clause : cnf) {
				Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
				
				if(clauseVars.containsAll(elementVars)) {
					elementHasRequiredVars = false;
					break;
				}
			}
			
			if(!elementHasRequiredVars) {
				continue;
			}
			
			args.add(optimize(element, cnf));
		}
		
		OpDisjunction result = OpDisjunction.create();
		result.getElements().addAll(args);
	
		return result;
	}

	public static Op _optimize(OpDistinct op, Set<Set<Expr>> cnf) {
		return new OpDistinct(optimize(op.getSubOp(), cnf));
	}

	public static Op _optimize(OpProject op, Set<Set<Expr>> cnf) {
		return new OpProject(optimize(op.getSubOp(), cnf), op.getVars());
	}
	
	public static Op _optimize(OpGroup op, Set<Set<Expr>> cnf) {
		return new OpGroup(optimize(op.getSubOp(), cnf), op.getGroupVars(), op.getAggregators());
	}
	
	//public static Op _optimize(OpEx)
	
	
	public static Op _optimize(OpFilter op, Set<Set<Expr>> cnf) {		
		Set<Set<Expr>> newCnf = CnfUtils.toSetCnf(op.getExprs());
		newCnf.addAll(cnf);
		
		return optimize(op.getSubOp(), newCnf);
	}

	public static Op _optimize(OpNull op, Set<Set<Expr>> cnf) 
	{
		return op;
	}
	
	public static Op _optimize(OpSlice op, Set<Set<Expr>> cnf)
	{
		return op.copy(optimize(op.getSubOp(), cnf));
	}
	
	public static Op _optimize(OpLeftJoin op, Set<Set<Expr>> cnf) {
		// Only push those expression on the, that do not contain any
		// variables of the right side
		
		Set<Var> rightVars = GetVarsMentioned.getVarsMentioned(op.getRight());
		
		
		Set<Set<Expr>> leftClauses = new HashSet<Set<Expr>>();
		Set<Set<Expr>> nonPushable = new HashSet<Set<Expr>>();

		for(Set<Expr> clause : cnf) {
			Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

			if(Sets.intersection(clauseVars, rightVars).isEmpty()) {
				leftClauses.add(clause);
			} else {
				nonPushable.add(clause);
			}
		}
		
		Op leftJoin = OpLeftJoin.create(optimize(op.getLeft(), leftClauses), optimize(op.getRight()), new ExprList());

		return surroundWithFilterIfNeccessary(leftJoin, nonPushable);
	}
	
	public static ExprList cnfToExprList(Set<Set<Expr>> cnf)
	{
		ExprList result = new ExprList();
		for(Set<Expr> clause : cnf) {
			Expr expr = ExprUtils.orifyBalanced(clause);
			result.add(expr);
		}
		
		return result;
	}

	public static Op surroundWithFilterIfNeccessary(Op op, Set<Set<Expr>> cnf)
	{
		if(cnf.isEmpty()) {
			return op;
		} else {
			ExprList exprs = cnfToExprList(cnf);
			
			Op result = OpFilter.filter(exprs, op);
			
			return result;
		}		
	}
	
	public static Op _optimize(OpRdfViewPattern op, Set<Set<Expr>> cnf) {
		return surroundWithFilterIfNeccessary(op, cnf);
	}

	/*
	public static Op _optimize(OpUnion op, Set<Set<Expr>> cnf) {
		Set<Var> rightVars = GetVarsMentioned.getVarsMentioned(op.getRight());
		
		
		Set<Set<Expr>> leftClauses = new HashSet<Set<Expr>>();
		Set<Set<Expr>> nonPushable = new HashSet<Set<Expr>>();

		for(Set<Expr> clause : cnf) {
			Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

			if(Sets.intersection(clauseVars, rightVars).isEmpty()) {
				leftClauses.add(clause);
			} else {
				nonPushable.add(clause);
			}
		}

		
		return new OpUnion(optimize(op.getLeft(), cnf), optimize(op.getRight(), cnf));
	}*/
	
}

