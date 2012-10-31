package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.core.algorithms.OpViewInstanceJoin;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.views.transform.GetVarsMentioned;
import org.apache.commons.collections15.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/**
 * A predicate that returns true if the given object is a subClass of a certain class.
 * Uses Class.isAssignableFrom.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
class PredicateInstanceOf<T>
	implements Predicate<T>
{
	private Class<?> superClass;
	
	public PredicateInstanceOf(Class<?> superClass) {
		this.superClass = superClass;
	}
	
	
	@Override
	public boolean evaluate(T value) {
		return value == null ? false : superClass.isAssignableFrom(value.getClass());
	}		
}

/**
 * 
 * @author raven
 *
 * Uses RestrictionManager for the filter expressions (indexed set of dnfs)
 *
 *
 * TODO: Inconsistent filters disappear
 */
public class FilterPlacementOptimizer2 {
	private static final Logger logger = LoggerFactory.getLogger(FilterPlacementOptimizer2.class);
	
	public static Op optimize(Op op) {
		RestrictionManagerImpl cnf = new RestrictionManagerImpl();
		return (Op)MultiMethod.invokeStatic(FilterPlacementOptimizer2.class, "_optimize", op, cnf);
	}

	
	public static Op optimize(Op op, RestrictionManagerImpl cnf) {
		if(op instanceof OpNull) {
			return op;
		}
		
		
		Op result = MultiMethod.invokeStatic(FilterPlacementOptimizer2.class, "_optimize", op, cnf);
		return result;
	}
	
	
	public static RestrictionManagerImpl filterByVars(RestrictionManagerImpl cnf, Op op) {	
		Set<Clause> clauses = cnf.getClausesForVars(GetVarsMentioned.getVarsMentioned(op));
		return new RestrictionManagerImpl(new NestedNormalForm(clauses));
	}

	public static Op _optimize(OpOrder op, RestrictionManagerImpl cnf) {
		return new OpOrder(optimize(op.getSubOp(), cnf), op.getConditions());
	}
	
	
	public static Op _optimize(OpJoin op, RestrictionManagerImpl cnf) {
		
		RestrictionManagerImpl leftCnf = filterByVars(cnf, op.getLeft());
		RestrictionManagerImpl rightCnf = filterByVars(cnf, op.getRight());
		
		Set<Clause> union = Sets.union(leftCnf.getCnf(), rightCnf.getCnf());
		Set<Clause> remaining = Sets.difference(cnf.getCnf(), union);

		Op result = OpJoin.create(optimize(op.getLeft(), leftCnf), optimize(op.getRight(), rightCnf));
		
		if(!remaining.isEmpty()) {
			//result = OpFilter.filter(cnfToExprList(remaining), result);
			result = OpFilterIndexed.filter(new RestrictionManagerImpl(new NestedNormalForm(remaining)), result);
		}
		
		return result;
	}
	
	

	// TODO This method looks wrong
	// For each element of the union push all appropriate clauses
	public static Op _optimize(OpDisjunction op, RestrictionManagerImpl cnf)
	{
		List<Op> args = new ArrayList<Op>();
		for(Op element : op.getElements()) {
			Set<Var> elementVars = GetVarsMentioned.getVarsMentioned(element);

			//Set<Clause> clauses = new HashSet<Clause>();
			
			boolean elementHasRequiredVars = true;
			for(Clause clause : cnf.getCnf()) {
				Set<Var> clauseVars = clause.getVarsMentioned();
				
				if(clauseVars.containsAll(elementVars)) {
					elementHasRequiredVars = false;
					break;
				}
			}
			
			if(!elementHasRequiredVars) {
				continue;
			}
			
			Op optimizedMember = optimize(element, cnf);
			args.add(optimizedMember);
		}
		
		OpDisjunction result = OpDisjunction.create();
		result.getElements().addAll(args);
	
		return result;
	}


	public static Op _optimize(OpDistinct op, RestrictionManagerImpl cnf) {
		return new OpDistinct(optimize(op.getSubOp(), cnf));
	}

	public static Op _optimize(OpProject op, RestrictionManagerImpl cnf) {
		return new OpProject(optimize(op.getSubOp(), cnf), op.getVars());
	}
	
	public static Op _optimize(OpExtend op, RestrictionManagerImpl cnf) {
		logger.warn("OpExtend probably not optimally implemented");
		return op.copy(optimize(op.getSubOp(), cnf));
	}
	
	public static Op _optimize(OpGroup op, RestrictionManagerImpl cnf) {
		return new OpGroup(optimize(op.getSubOp(), cnf), op.getGroupVars(), op.getAggregators());
	}
	
	//public static Op _optimize(OpEx)
	
	
	/*
	public static Op _optimize(OpFilter op, RestrictionManager cnf) {
		RestrictionManager child = new RestrictionManager(cnf);
		

		for(Expr expr : op.getExprs()) {
			NestedNormalForm newCnf = CnfUtils.toCnf(expr);
			child.stateCnf(newCnf);
		}
		
		return optimize(op.getSubOp(), child);
	}
	*/
	public static Op _optimize(OpFilterIndexed op, RestrictionManagerImpl cnf) {
		
		RestrictionManagerImpl child = new RestrictionManagerImpl(cnf);
		

		child.stateRestriction(op.getRestrictions());

		/*
		if(child.isUnsatisfiable()) {
			 Op result = OpNull.create();
			 return result;
		}*/

		
		Op result = optimize(op.getSubOp(), child);
		return result;
	}

	public static Op _optimize(OpNull op, RestrictionManagerImpl cnf) 
	{
		return op;
	}
	
	public static Op _optimize(OpSlice op, RestrictionManagerImpl cnf)
	{
		return op.copy(optimize(op.getSubOp(), cnf));
	}
	
	public static boolean evalPredicate(Expr expr, Predicate<Expr> predicate) {
		if(predicate.evaluate(expr)) {
			return true;
		} else if(expr.isFunction()) {
			for(Expr arg : expr.getFunction().getArgs()) {
				if(evalPredicate(arg, predicate)) {
					return true;
				}
			}
		}
		
		return false;
	}
	

	
	public static boolean doesClauseContainBoundExpr(Clause clause) {
		
		Predicate<Expr> predicate = new PredicateInstanceOf<Expr>(E_Bound.class);

		/*
		Expr test = new E_Bound(new ExprVar(Var.alloc("v")));
		System.out.println("Predicate evaluated to: " + predicate.evaluate(test));
		System.exit(0);
		*/
		
		for(Expr expr : clause.getExprs()) {
			if(evalPredicate(expr, predicate)) {
				return true;
			}
		}

		return false;
	}
	
	
	public static Op _optimize(OpLeftJoin op, RestrictionManagerImpl cnf) {		
		// Only push those expression on the, that do not contain any
		// variables of the right side
		
		Set<Var> rightVars = GetVarsMentioned.getVarsMentioned(op.getRight());
		
		
		Set<Clause> leftClauses = new HashSet<Clause>();
		Set<Clause> nonPushable = new HashSet<Clause>();

		for(Clause clause : cnf.getCnf()) {
			Set<Var> clauseVars = clause.getVarsMentioned();


			if(Sets.intersection(clauseVars, rightVars).isEmpty()) { //  Do we need to check && !doesClauseContainBoundExpr(clause)) {				
				leftClauses.add(clause);
			} else {
				nonPushable.add(clause);
			}
		}
		
		RestrictionManagerImpl left = new RestrictionManagerImpl(new NestedNormalForm(leftClauses));
		RestrictionManagerImpl np = new RestrictionManagerImpl(new NestedNormalForm(nonPushable));
		
		Op leftJoin = OpLeftJoin.create(optimize(op.getLeft(), left), optimize(op.getRight(), left), new ExprList());

		Op result = surroundWithFilterIfNeccessary(leftJoin, np);
		return result;
	}
	
	/*
	public static ExprList cnfToExprList(Set<Set<Expr>> cnf)
	{
		ExprList result = new ExprList();
		for(Set<Expr> clause : cnf) {
			Expr expr = ExprUtils.orifyBalanced(clause);
			result.add(expr);
		}
		
		return result;
	}*/

	public static Op surroundWithFilterIfNeccessary(Op op, RestrictionManagerImpl cnf)
	{
		Op result;

		if(cnf.isUnsatisfiable()) {
			result = new OpFilterIndexed(op, new RestrictionManagerImpl(new NestedNormalForm(new HashSet<Clause>(Collections.singleton(new Clause(new HashSet<Expr>(Collections.singleton(NodeValue.FALSE))))))));
		}
		else if(cnf.getCnf().isEmpty()) {
			result = op;
		}
		else {
			result = new OpFilterIndexed(op, cnf);
			/*
			ExprList exprs = cnfToExprList(cnf);
			
			Op result = OpFilter.filter(exprs, op);
			*/			
		}
		
		return result;
	}

	@Deprecated
	public static Op _optimize(OpRdfViewPattern op, RestrictionManagerImpl cnf) {
		return surroundWithFilterIfNeccessary(op, cnf);
	}

	public static Op _optimize(OpViewInstanceJoin op, RestrictionManagerImpl cnf) {
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

