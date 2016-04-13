package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.FlatMapView;

import sparql.DnfUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;



class DnfIndex {
	private Multimap<Var, Expr> singleVarExpr = HashMultimap.create(); 
	//private Map<Clause> specific 
	
	
	// NOTE: Additions are conjunctive!
	public void add(Dnf dnf) {
		Collection<Clause> clauses = dnf.getClauses();
		
		//dnf.getCommonExprs()
	}
	
}


/**
 * 
 * TODO Clarify: Do I want disjunctive or conjunctive normal form?
 * (a and b) or (c and d) or
 * (a or b) and(c or d)   ?
 * I guess the former is better, as (a and not a) can be easily detected
 * 
 * What happens if we allow conjunctions of disjunctive normal forms?
 * ((a, b) or (c, d) AND ((x) or (y)))
 * becomes
 * (abx or aby or cdx or cdy) (so the cross product of the involved clauses)
 *
 * Maybe i could do virtual clauses:
 * CrossJoinClause(Clauses ...)
 * 
 * 
 * 
 * Hm, but with CNF it is easier to add new expressions. 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprIndex {

	private ExprIndex parent;
	
	
	// TODO I need this method due to the lack of suppert for CNF lookups on tables right now
	public void getEffectiveDnf(Collection<Var> vars) {
	}
	
	/**
	 * I use this method for getting constraints for finding view candidates
	 * 
	 * 
	 * @param dnfs
	 * @param index
	 * @param dnfIndex
	 * @param blacklist
	 * @param depth
	 * @param parentClause
	 * @param result
	 */
	public void calcEffectiveClauses(final List<Dnf> dnfs, int index, final Set<Clause> blacklist, Clause parentClause, final Set<Clause> result) {
		if(index >= dnfs.size()) {
			if(parentClause != null) {
				result.add(parentClause);
			}
			
			return;
		}
		
		Dnf dnf = dnfs.get(index);

		for(Clause clause : dnf.getClauses()) {
			if(blacklist.contains(clause)) {
				continue;
			}
			
			Clause merged;
			if(parentClause == null) {
				merged = parentClause;
			} else {
				Set<Expr> exprs = new HashSet<Expr>(parentClause.getExprs());
				exprs.addAll(clause.getExprs());
				merged = new Clause(exprs);
				

				calcEffectiveClauses(dnfs, index + 1, blacklist, merged, result);
			}			
		}
	}
	
	/**
	 * This method is an ugly hack right, but I don't know
	 * where to better calculate it
	 * 
	 * @return
	 */
	public Set<Clause> calcEffectiveClauses() {
		Set<Clause> result = new HashSet<Clause>();
		
		List<Collection<Clause>> tmp = new ArrayList<Collection<Clause>>();
		
		for(Dnf dnf : dnfs) {
			tmp.add(dnf.getClauses());
		}
		
		CartesianProduct<Clause> cartesian = CartesianProduct.create(tmp);

		for(List<Clause> list : cartesian) {
			Set<Expr> exprs = new HashSet<Expr>();

			for(Clause c : list) {
				exprs.addAll(c.getExprs());
			}
			
			result.add(new Clause(exprs));
		}
		
		return result;
	}
	
	public Set<Clause> getEffectiveClauses() {
		return effectiveClauses;
	}
	
	
	private Set<Clause> effectiveClauses;
	
	
	//private List<Dnf> dnfs = new ArrayList<Dnf>();
	// The DNFs are sorted by number of clauses
	private NavigableMap<Integer, Set<Dnf>> dnfGroups = new TreeMap<Integer, Set<Dnf>>();
	
	
	private Collection<Dnf> dnfs = new FlatMapView<Dnf>(dnfGroups.values());
	
	private Set<Var> varsMentioned = new HashSet<Var>();
	
	
	//private Map<Var, Expr> 
	//private Map<Var, Expr> commonSingleVarExprs
	
	
	//private Dnf dnf = new Dnf();
	//private Map<Var, Clause> varToClauses = new HashMap<Var, Clause>();
	//private ExprList exprs = new ExprList();
	//private Multimap<Var, Expr> varToExprs = HashMultimap.create();
	//VarExprList x;
	//private Map<Var, PrefixSet> varToPrefixes = new HashMap<Var, PrefixSet>();
	//private Map<Var>
	
	public ExprIndex() {
		
	}
	

	public ExprIndex(ExprIndex parent) {
		this.parent = parent;
		/*
		for(Dnf dnf : parent.getDnf()) {
			add(dnf);
		}*/
	}
	
	public ExprIndex(ExprIndex parent, Iterable<Expr> exprs) {
		this.parent = parent;
		addAll(exprs);
	}
	
	public ExprIndex(Set<Dnf> dnfs) {
		addAll(dnfs);
	}
	
	public ExprIndex getParent() {
		return parent;
	}
	
	public Collection<Dnf> getDnf() {
		return dnfs;
	}
	
	public Set<Var> getVarsMentioned() {
		return varsMentioned;
	}
	
	void add(Dnf dnf) {
		int n = dnf.getClauses().size();
		Set<Dnf> set = dnfGroups.get(n);
		if(set == null) {
			set = new HashSet<Dnf>();
			dnfGroups.put(n, set);
		}
		set.add(dnf);
		
		varsMentioned.addAll(dnf.getVarsMentioned());
		
		this.effectiveClauses = calcEffectiveClauses();
	}
	
	void addAll(Collection<Dnf> dnfs) {
		for(Dnf dnf : dnfs) {
			add(dnf);
		}
	}

	void add(Expr expr) {
		Dnf dnf = DnfUtils.toDnf(expr);
		
		add(dnf);
		//dnfs.add(dnf);
	
		/*
		exprs.add(expr);
		for(Var var : exprs.getVarsMentioned()) {
			varToExprs.put(var, expr);
		}*/
		
		//DnfUtils.toDnf(expr);
		//varToExp
	}
	
	
		
	/**
	 * Any clause containing a var means, that in order for the clause to evaluate
	 * to true, the expression it is involved must be true.
	 * 
	 * @param var
	 * @return
	 */
	public Set<Clause> getAllClausesWith(Var var) {
		Set<Clause> result = new HashSet<Clause>();
		
		for(Set<Dnf> dnfGroup : dnfGroups.values()) {
			for(Dnf dnf : dnfGroup) {
				Collection<Clause> clauses = dnf.get(var);
				
				result.addAll(clauses);
			}
		}
		
		return result;
	}
	
	public Set<Clause> getAllSingleVarExprs(Var var) {
		Set<Clause> result = new HashSet<Clause>();
		
		for(Set<Dnf> dnfGroup : dnfGroups.values()) {
			for(Dnf dnf : dnfGroup) {
				Collection<Clause> clauses = dnf.get(Collections.singleton(var));
				
				result.addAll(clauses);
			}
		}
		
		return result;
	}
	
	
	
	public void addAll(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			add(expr);
		}
	}
	
	@Override
	public String toString() {
		return dnfs.toString();
	}

	
	public ExprIndex filterByVars(Set<Var> requiredVars) {
		//List<Dnf> filteredDnfs = new ArrayList<Dnf>();
		ExprIndex result = new ExprIndex();
		for(Set<Dnf> dnfGroup : dnfGroups.values()) {
			for(Dnf dnf : dnfGroup) {
				Set<Clause> clauses = dnf.filterByVars(requiredVars);
				Dnf filteredDnf = new Dnf(clauses);
				result.add(filteredDnf);
			}
		}
		
		return result;
	}
}





/*
class EffectiveClauseIterator
	extends SinglePrefetchIterator<Clause>
{
	//private ExprIndex dnfIndex;
	private Set<Clause> blacklist;
	private StackCartesianProductIterator<Clause> it;
	
	public EffectiveClauseIterator(ExprIndex dnfIndex, Set<Clause> blacklist) {
		Collection<Dnf> dnfs = dnfIndex.getDnf();
		
		List<Collection<Clause>> clauses = new ArrayList<Collection<Clause>>();
		for(Dnf dnf : dnfs) {
			clauses.add(dnf.getClauses());
		}
		
		//DescenderIterator<Clause> it = new DescenderIterator<Clause>(clauses, )
		this.it = new StackCartesianProductIterator<Clause>(clauses);
	}

	@Override
	protected Clause prefetch() throws Exception {
		while(it.hasNext()) {
			List<Clause> item = it.next();
			
			Clause newClause = item.get(item.size() - 1);
			
			if(blacklist.contains(newClause)) {
				continue;
			} else if(it.canDescend()) {
				it.descend();
			}
		}
	}
	
	
}
*/



/**
 * A class intended for keeping track of possible constant values of a variable.
 * 
 * Do I have to work with clauses? Or is it possible to simplify?
 * 
 * OK, I don't think this class makes sense right now.
 * If we map a variable to a constant, then we don't need to deal with alternatives.
 * And if we want to mess around with expressions and satisfiability, then we can just
 * work on the expression level, rather trying to destill possible constant assignments from the exprs
 * (althought it might be useable for optimization) 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
class ValueSet<T>
{
	private Set<Set<T>> alternatives;
	
	/**
	 * Example 1: Alternatives, or
	 * (a = x || a = y) 
	 *  ->  ((x), (y))
	 * 
	 * ... && (a = x || a = z)
	 *  -> a = x
	 * 
	 *
	 * 
	 * Example 2: Negated alternatives
	 * (?a != y || ?a != z) -> no constraint derivable
	 * "x is either not an Animal or it is not a Person"  
	 *  ((!y), (!z))
	 * 
	 * 
	 * Example 3: Negative alternatives
	 * (?a != y && ?a != z) 
	 * "x is neither an Animal nor a Person"
	 *     -> neg = (!y, !z) 
	 * 
	 * Example 4: Mixed
	 * (?a = x || ?a = y || ?a != y || ?a != z)
	 * 
	 *
	 * Example 5: Mixed
	 * (?a = v || ?a = w) && (?a != v && ?a != y)
	 *     -> (?a = v && ?a != v && ?a != y) || (?a = w && ?a != v && ?a != y)
	 *     -> (false) || (?a = w && ?a != v && ?a != y)
	 * pos = {w}  
	 * 
	 * 
	 * So stating an equality  
	 * 
	 * @param values
	 */
	public void stateAlternatives(Set<T> values) {
		
	}
	
	
	
	
}


