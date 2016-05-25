package sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.IterableCollection;
import org.aksw.commons.collections.Sample;
import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Sets;




public class FilterUtils
{
	public static Expr determineFilterExpr(Quad quad, Set<Set<Expr>> dnf)
	{
		return DnfUtils.dnfToExpr(determineFilterDnf(quad, dnf), true);
	}
	

	public static Pair<Var, NodeValue> extractValueConstraint(Expr expr)
	{
		if(!(expr instanceof ExprFunction2)) {
			return null;
		}
		
		ExprFunction func = (ExprFunction)expr;
		
		
		Expr a = func.getArg(1);
		Expr b = func.getArg(2);
		
		if(a.isVariable() == b.isVariable()) {
			return null;
		}
		
		if(b.isVariable()) {
			Expr tmp = b;
			b = a;
			a = tmp;
		}
		
		if(!b.isConstant()) {
			return null;
		}
		
		return Pair.create(a.asVar(), b.getConstant());
		
	}
	
	
	/**
	 * Returns true if it was cleaned, or false if the clause was unsatisfiable
	 * 
	 * @param sample
	 * @return
	 */
	public static ValueSet<NodeValue> toValueSet(Sample<NodeValue> sample)
	{
		if(sample.getPositives().size() > 1 || (!sample.getPositives().isEmpty() && sample.getNegatives().containsAll(sample.getPositives()))) {
			/**
			 * If there are
			 * .) multiple positive constants in a clause, such as in [?x = a, ?x = b]
			 * .) or the negatives contain the positive, such as in [?x = a, !(?x = a)]
			 * then the clause is unsatisfiable and no value contraint can be derived 
			 */
			return ValueSet.create();

		}
		
		if(!sample.getPositives().isEmpty()) {
			/**
			 * Just in case deal with the case [?x = a, !(?x = b)] ---> [?x = a]
			 * 
			 * So positive takes precedence over negative
			 */					
			sample.getNegatives().clear();
		}
		
		return sample.getPositives().isEmpty()
			? ValueSet.create(sample.getNegatives(), false)
			: ValueSet.create(sample.getPositives(), true);
	}
	                           
	
	/**
	 * Searches for expression of the form ?var = const
	 * 
	 * TODO We are not considering equals between variables.
	 * A space efficient way to achieve this is by
	 * creating a new expression, where the substitution ?x ?y has been applied.
	 * 
	 * ?x = const . ?y = const . ?x = ?y .
	 * 
	 * @param clause
	 * @return
	 */
	public static Map<Var, ValueSet<NodeValue>> extractValueConstraintsDnf(Set<Set<Expr>> dnf)
	{		
		Map<Var, ValueSet<NodeValue>> result = null;
	
		if(dnf == null) {
			return result;
		}

		
		for(Set<Expr> clause : dnf) {
			Map<Var, ValueSet<NodeValue>> map = extractValueConstraintsDnfClause(clause);

			if(result == null) {
				result = map;
				continue;
			}
			
			Iterator<Map.Entry<Var, ValueSet<NodeValue>>> itEntry = result.entrySet().iterator();
			//for(Map.Entry<Var, ValueSet<NodeValue>> entry : result.entrySet()) {
			while(itEntry.hasNext()) {
				Map.Entry<Var, ValueSet<NodeValue>> entry = itEntry.next();
				
				ValueSet<NodeValue> a = entry.getValue();
				ValueSet<NodeValue> b = map.get(entry.getKey());
				
				a.merge(b);
				
				if(a.isUnknown()) {
					itEntry.remove();
				}
			}
			
			if(result.isEmpty()) {
				break;
			}
		}
		
		return (result == null) ? new HashMap<Var, ValueSet<NodeValue>>() : result; 
	}
	
	
	public static void addValueToSample(Sample<NodeValue> sample, NodeValue nodeValue, boolean isNegative)
	{
	}
	
	/**
	 * Intersection where set 'a' may be null.
	 * In that case the result is 'b'
	 * Useful for cases where initally a set is unconstrained (contains all elements)
	 * 
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> Set<T> intersection(Set<T> a, Set<T> b)
	{
		return (a == null)
			? (b == null ? null : new HashSet<T>(b))
			: new HashSet<T>(Sets.intersection(a, b));
	}

	public static <T> Set<T> union(Set<T> a, Set<T> b)
	{
		return (a == null)
			? (b == null ? null : new HashSet<T>(b))
			: new HashSet<T>(Sets.union(a, b));
	}

	
	
	public static Map<Var, ValueSet<NodeValue>> extractValueConstraintsDnfClause(Set<Expr> dnfClause)
	{
		Map<Var, Sample<NodeValue>> tmp = new HashMap<Var, Sample<NodeValue>>();

		for(Expr expr : dnfClause) {

			boolean isNegative = (expr instanceof E_LogicalNot);
			if(isNegative) {
				expr = ((E_LogicalNot)expr).getArg();
			}

			if(!(expr instanceof E_Equals)) {
				continue;
			}
				
			Pair<Var, NodeValue> constraint = extractValueConstraint(expr);

			if(constraint == null) {
				continue;
			}
			
			
			Sample<NodeValue> sample = tmp.get(constraint.getKey());
			if(sample == null) {
				sample = Sample.create();
				tmp.put(constraint.getKey(), sample);
			}

			
			if(isNegative) {
				sample.getNegatives().add(constraint.getValue());
			} else {
				sample.getPositives().add(constraint.getValue());			
			}
		}
		
		
		Map<Var, ValueSet<NodeValue>> result = new HashMap<Var, ValueSet<NodeValue>>();  

		// Phase 2: Create the value sets
		for(Map.Entry<Var, Sample<NodeValue>> entry : tmp.entrySet()) {
			ValueSet<NodeValue> valueSet = toValueSet(entry.getValue());
			
			if(valueSet.isUnknown()) {
				continue;
			}
			
			result.put(entry.getKey(), valueSet);
		}
		
		
		return result;
	}

	
	/**
	 * Given a quad and a predicate expression in DNF,
	 * determine if any of the expressions only containing variables of the quad
	 * are a "notwendige bedingung" on the pattern.
	 * 
	 * What i mean is: If the extracted predicate fails for a binding of the variables on the triple,
	 * it will also fail for the whole expression.
	 * 
	 * Technically we do the following:
	 * In the first phase we check which variables of the quad appear in all clauses.
	 * In the second phase we extract only those expressions containing the remaining variables from the first phase.
	 * 
	 * 
	 * TODO Currently we treat null as an always 'true' dnf
	 * 
	 * @param quad
	 * @param dnf
	 * @return
	 */
	public static Set<Set<Expr>> determineFilterDnf(Quad quad, Set<Set<Expr>> dnf)
	{
		if(dnf == null) {
			return null;
		}
			
		
		Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);
		
		for(Set<Expr> clause : dnf) {
			quadVars.retainAll(ClauseUtils.getVarsMentioned(clause));
		}
		
		Set<Set<Expr>> result = null; 
		
		if(quadVars.isEmpty()) {
			return result;
		}
		
		for(Set<Expr> clause : dnf) {
			//Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
			
			Set<Expr> c = null;
			for(Expr expr : clause) {
				if(quadVars.containsAll(expr.getVarsMentioned())) {
					if(c == null) {
						c = new HashSet<Expr>();
					}
					
					c.add(expr);
				}
			}
			
			if(c != null) {
				if(result == null) {
					result = new HashSet<Set<Expr>>();
				}
				result.add(c); 
			}
		}
		
		return result;
	}
	
	
	/**
	 * A test to check if the given predicate expression would yield false
	 * with the given, potentially partial, binding.
	 * 
	 * Expressions making use of unbound variables will be considered
	 * satisfiable.
	 * 
	 * Note this method is intended to be used as a hint in optizations.
	 * The assumption is that no code breaks if always false is retured.
	 * 
	 * @param expr
	 * @param partialBinding
	 * @return
	 */
	public boolean isUnsatsfiable(Expr expr, Binding partialBinding)
	{
		return false;
	}
	
	
	public static Set<Set<Expr>> toSets(List<ExprList> clauses) 
	{
		if(clauses == null) {
			return null;
		}
		
		Set<Set<Expr>> result = new HashSet<Set<Expr>>();
		
		for(ExprList clause : clauses) {
			result.add(new HashSet<Expr>(IterableCollection.wrap(clause)));
		}
		
		return result;
	}


	public static ExprList collectExprs(Op op, ExprList result) {
		if(op instanceof OpLeftJoin) {
			OpLeftJoin x = (OpLeftJoin)op;
			
			if(x.getExprs() != null) {
				result.addAll(x.getExprs());
			}
			
			collectExprs(x.getLeft(), result);
			collectExprs(x.getRight(), result);
		} else if(op instanceof OpFilter) {
			OpFilter x = (OpFilter)op;
			result.addAll(x.getExprs());
			collectExprs(x.getSubOp(), result);
		} else if(op instanceof OpJoin) {
			OpJoin x = (OpJoin)op;
			
			collectExprs(x.getLeft(), result);
			collectExprs(x.getRight(), result);
		} else if(op instanceof OpUnion) {
			System.out.println("Warning: Collecting expressions from unions. Since the same vars may appear within different (parts of) unions, it may be ambiguous to which part the expression refers.");
	
			OpUnion x = (OpUnion)op;
	
			collectExprs(x.getLeft(), result);
			collectExprs(x.getRight(), result);
		} else if(op instanceof OpQuadPattern) {
	
		} else if(op instanceof OpSequence) {
			OpSequence x = (OpSequence)op;
			for(Op element : x.getElements()) {
				collectExprs(element, result);
			}			
		} else {
			throw new NotImplementedException("Type: " + op.getClass());
		}
		
		return result;
	}


	/**
	 * Return true if a is subsumed by b which means
	 * whenever a is true, b will be true 
	 *
	 * You may need
	 * Expr x = originalExpr.copySubstitute(binding);
	 * to rename the variables and thus make the expressions equal.
	 * 
	 * Note: This method only gives an hint when a subsumption is found.
	 * If it returns false it does not neccessarily mean that there is none.
	 * 
	 * (b(x) -> true) implied (a(x) -> true)
	 * 
	 * or(and(a, b), and(b, c)
	 * 
	 * The current implementation only checks if the constraints of a more
	 * restrictive than those of b:
	 * 
	 * A is subsumed by B if for each clause in A there exists a clause in B 
	 * that contains A.
	 * 
	 * This method so far deos NOT do fancy stuff such as finding out that
	 * ?x InRange (2, 4) is subsumed by ?y InRange (1, 5)  
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isDnfSubsumed(Set<Set<Expr>> clausesA, Set<Set<Expr>> clausesB)
	{
		for(Set<Expr> clauseA : clausesA) {				
			
			boolean isClauseSubsumed = false;
			for(Set<Expr> clauseB : clausesB) {
				boolean tmp = isDnfClauseSubsumed(clauseA, clauseB);
				isClauseSubsumed = isClauseSubsumed || tmp;
			}
			
			if(isClauseSubsumed == false) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isDnfClauseSubsumed(Set<Expr> clauseA, Set<Expr> clauseB) {
		return clauseB.containsAll(clauseA);
	}
	
}
