package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.factory.Factory2;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.core.algorithms.OpMapping;
import org.aksw.sparqlify.core.algorithms.OpViewInstanceJoin;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.sparqlview.OpSparqlViewPattern;
import org.aksw.sparqlify.views.transform.GetVarsMentioned;
import org.apache.commons.collections15.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;


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
	
    public static Factory2<Op> joinFactory = new Factory2<Op>() {
        @Override
        public Op create(Op a, Op b) {
            Op result = OpJoin.create(a, b);
            return result;
        }
    
    };

	
	public static Op optimize(Op op) {
		RestrictionManagerImpl cnf = new RestrictionManagerImpl();
		Op result = MultiMethod.invokeStatic(FilterPlacementOptimizer2.class, "_optimize", op, cnf);
		return result;
	}

	
	public static Op optimize(Op op, RestrictionManagerImpl cnf) {
//		if(op instanceof OpNull) {
//			return op;
//		}
		
		
		Op result = MultiMethod.invokeStatic(FilterPlacementOptimizer2.class, "_optimize", op, cnf);
		return result;
	}
	
	
	public static RestrictionManagerImpl filterByVars(RestrictionManagerImpl cnf, Op op) {	
		Set<Var> vars = GetVarsMentioned.getVarsMentioned(op);
		Set<Clause> clauses = cnf.getClausesForVars(vars);
		return new RestrictionManagerImpl(new NestedNormalForm(clauses));
	}

	public static Op _optimize(OpOrder op, RestrictionManagerImpl cnf) {
		return new OpOrder(optimize(op.getSubOp(), cnf), op.getConditions());
	}

	public static Op _optimize(OpTopN op, RestrictionManagerImpl cnf) {
		return new OpTopN(optimize(op.getSubOp(), cnf), op.getLimit(), op.getConditions());
	}

	
	
	public static Op _optimize(OpJoin op, RestrictionManagerImpl cnf) {
        
        Op result = handleLeftJoin(op.getLeft(), op.getRight(), cnf, joinFactory);
        return result;      
	    
	}
	
	public static Op _optimizeBreaking(OpJoin op, RestrictionManagerImpl cnf) {
		
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
	

//	public static Op _optimize(OpJoin op, RestrictionManagerImpl cnf) {		
//		RestrictionManagerImpl leftCnf = filterByVars(cnf, op.getLeft());
//		RestrictionManagerImpl rightCnf = filterByVars(cnf, op.getRight());
//		
//		Set<Clause> union = Sets.union(leftCnf.getCnf(), rightCnf.getCnf());
//		Set<Clause> remaining = Sets.difference(cnf.getCnf(), union);
//
//		Op result = OpJoin.create(optimize(op.getLeft(), leftCnf), optimize(op.getRight(), rightCnf));
//		
//		if(!remaining.isEmpty()) {
//			//result = OpFilter.filter(cnfToExprList(remaining), result);
//			result = OpFilterIndexed.filter(new RestrictionManagerImpl(new NestedNormalForm(remaining)), result);
//		}
//		
//		return result;
//	}

	public static Op _optimize(OpSequence op, RestrictionManagerImpl cnf) {
		List<Op> members = op.getElements();
		
		List<Op> newMembers = new ArrayList<Op>(members.size());
		Set<Clause> intersection = new HashSet<Clause>();

		for(Op member : members) {			
			RestrictionManagerImpl restrictions = filterByVars(cnf, member);
			Op newMember = optimize(member, restrictions);
			newMembers.add(newMember);

			Set<Clause> tmp = Sets.intersection(restrictions.getCnf(), intersection);
			intersection = new HashSet<Clause>(tmp);
		}
		Set<Clause> remaining = Sets.difference(cnf.getCnf(), intersection);
				
		Op result = OpSequence.create().copy(newMembers);
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
		Op subOp = optimize(op.getSubOp(), cnf);
		Op result = new OpProject(subOp, op.getVars());
		return result;
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
	
	public static Op _optimizeNewButNotSureIfWeNeedSplitsHere(OpFilterIndexed op, RestrictionManagerImpl cnf) {
		
		RestrictionManagerImpl child = new RestrictionManagerImpl(cnf);
		child.stateRestriction(op.getRestrictions());

		
		FilterSplit filterSplit = splitFilter(op, child);
		
		
		RestrictionManagerImpl pushable = filterSplit.getPushable();
		

		Op result = optimize(op.getSubOp(), pushable);

		
		if(!filterSplit.getNonPushable().getCnf().isEmpty()) {
			result = OpFilterIndexed.filter(filterSplit.getNonPushable(), result);
		}
		
		/*
		if(child.isUnsatisfiable()) {
			 Op result = OpNull.create();
			 return result;
		}*/

		
		return result;
	}
	
	
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
	
	
	
	public static Op _optimize(final OpLeftJoin op, RestrictionManagerImpl cnf) {
	
		Factory2<Op> factory = new Factory2<Op>() {
			@Override
			public Op create(Op a, Op b) {
				Op result = OpLeftJoin.create(a, b, op.getExprs());
				return result;
			}
		
		};
		
		Op result = handleLeftJoin(op.getLeft(), op.getRight(), cnf, factory);
		return result;		
	}

	public static Op _optimize(OpConditional op, RestrictionManagerImpl cnf) {
		Factory2<Op> factory = new Factory2<Op>() {
			@Override
			public Op create(Op a, Op b) {
				Op result = new OpConditional(a, b);
				return result;
			}
		
		};

		Op result = handleLeftJoin(op.getLeft(), op.getRight(), cnf, factory);
		return result;
	}
	
	
	public static FilterSplit splitFilter(Op op, RestrictionManagerImpl cnf) {
		//Set<Var> rightVars = GetVarsMentioned.getVarsMentioned(right);
		
		Set<Var> opVars = GetVarsMentioned.getVarsMentioned(op);
		
		Set<Clause> leftClauses = new HashSet<Clause>();
		Set<Clause> nonPushable = new HashSet<Clause>();

		for(Clause clause : cnf.getCnf()) {
			Set<Var> clauseVars = clause.getVarsMentioned();

			// If the clause contains vars that are not part of the op, we cannot push it down
			if(opVars.containsAll(clauseVars)) {
				leftClauses.add(clause);				
			} else {
				nonPushable.add(clause);				
			}

			/*
			if(Sets.intersection(clauseVars, rightVars).isEmpty()) { //  Do we need to check && !doesClauseContainBoundExpr(clause)) {				
				leftClauses.add(clause);
			} else {
				nonPushable.add(clause);
			}
			*/
		}
		
		RestrictionManagerImpl leftRm = new RestrictionManagerImpl(new NestedNormalForm(leftClauses));
		RestrictionManagerImpl np = new RestrictionManagerImpl(new NestedNormalForm(nonPushable));
		
		for(Entry<Var, RestrictionImpl> entry : cnf.getRestrictions().entrySet()) {
			Var var = entry.getKey();
			RestrictionImpl rest = entry.getValue();
			
			leftRm.stateRestriction(var, rest);
			np.stateRestriction(var, rest);
		}

		/*
		for(Var var : leftRm.getVariables()) {
			RestrictionImpl r = cnf.getRestriction(var);
			if(r != null) {
				leftRm.stateRestriction(var, r);
			}
		}
		
		for(Var var : np.getVariables()) {
			RestrictionImpl r = cnf.getRestriction(var);
			if(r != null) {
				np.stateRestriction(var, r);
			}
		}
		*/
		
		
		FilterSplit result = new FilterSplit(leftRm, np);
		
		return result;
	}

	/*
	 * TODO: We could still push constraints down on the RHS - why aren't we doing this?
	 * Even the old version only considered restrictions on the left hand side
	 * 
	 */
	public static Op handleLeftJoin(Op left, Op right, RestrictionManagerImpl cnf, Factory2<Op> factory) {
		// Only push those expression on the, that do not contain any
		// variables of the right side
		
		FilterSplit filterSplit = splitFilter(left, cnf);
		
		RestrictionManagerImpl leftRm = filterSplit.getPushable();
		RestrictionManagerImpl np = filterSplit.getNonPushable();
		
		Op newLeft = optimize(left, leftRm);
		
		// We can push expressions from the left side into the right side - but
		// only if ther expressions are pushable
		FilterSplit rsplit = splitFilter(right, leftRm);
		RestrictionManagerImpl rightRm = rsplit.getPushable();
		Op newRight = optimize(right, rightRm);
		
		//Op leftJoin = OpLeftJoin.create(newLeft, newRight, new ExprList());
		Op leftJoin = factory.create(newLeft, newRight);

		Op result = surroundWithFilterIfNeccessary(leftJoin, np);
		return result;
	}
	
	
	public static Op handleLeftJoinOld(Op left, Op right, RestrictionManagerImpl cnf, Factory2<Op> factory) {
		// Only push those expression on the, that do not contain any
		// variables of the right side
		
		Set<Var> rightVars = GetVarsMentioned.getVarsMentioned(right);
		
		
		Set<Clause> leftClauses = new HashSet<Clause>();
		Set<Clause> nonPushable = new HashSet<Clause>();

		for(Clause clause : cnf.getCnf()) {
			Set<Var> clauseVars = clause.getVarsMentioned();

			// If there are variables in the clause which do not appear on the right side, we cannot push the clause down the right side of the left join
			//if(rightVars.containsAll(clauseVars)) {
			if(Sets.intersection(clauseVars, rightVars).isEmpty()) {
				leftClauses.add(clause);				
			} else {
				nonPushable.add(clause);				
			}

			/*
			if(Sets.intersection(clauseVars, rightVars).isEmpty()) { //  Do we need to check && !doesClauseContainBoundExpr(clause)) {				
				leftClauses.add(clause);
			} else {
				nonPushable.add(clause);
			}
			*/
		}
		
		RestrictionManagerImpl leftRm = new RestrictionManagerImpl(new NestedNormalForm(leftClauses));
		RestrictionManagerImpl np = new RestrictionManagerImpl(new NestedNormalForm(nonPushable));
		
		Op newLeft = optimize(left, leftRm);
		Op newRight = optimize(right, leftRm);
		
		//Op leftJoin = OpLeftJoin.create(newLeft, newRight, new ExprList());
		Op leftJoin = factory.create(newLeft, newRight);

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

	public static Op _optimize(OpMapping op, RestrictionManagerImpl cnf) {
		return surroundWithFilterIfNeccessary(op, cnf);
	}

	
	public static Op _optimize(OpSparqlViewPattern op, RestrictionManagerImpl cnf) {
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

