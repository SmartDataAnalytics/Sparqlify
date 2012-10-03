package org.aksw.sparqlify.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.restriction.RestrictionImpl;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * A clause with restrictions derived for the variables
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class Clause
	extends ClauseBase
{
	private Map<Var, RestrictionImpl> varToRestriction = new HashMap<Var, RestrictionImpl>();
	
	public Clause(Set<Expr> exprs) {
		super(exprs);

		
		for(Expr expr : exprs) {
			if(expr instanceof E_Equals) {
				deriveRestrictionEquals((E_Equals)expr);
			}
		}
	}
	
	private RestrictionImpl getOrCreateRestriction(Var var) {
		RestrictionImpl result = varToRestriction.get(var);
		if(result == null) {
			result = new RestrictionImpl();
			varToRestriction.put(var, result);
		}
		return result;
	}
	
	private void deriveRestrictionEquals(E_Equals expr)
	{
		if(!deriveRestrictionEquals(expr.getArg1(), expr.getArg2())) {
			deriveRestrictionEquals(expr.getArg2(), expr.getArg1());
		}		
	}
	
	private boolean deriveRestrictionEquals(Expr a, Expr b)
	{
		if(a.isVariable() && b.isConstant()) {
			RestrictionImpl r = getOrCreateRestriction(a.asVar());
			r.stateNode(b.getConstant().asNode());
			
			return true;
		}
		
		return false;
	}
	
	public RestrictionImpl getRestriction(Var var) {
		return varToRestriction.get(var);
	}
	
	public Map<Var, RestrictionImpl> getRestrictions() {
		return varToRestriction;
	}

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((varToRestriction == null) ? 0 : varToRestriction.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Clause other = (Clause) obj;
		if (varToRestriction == null) {
			if (other.varToRestriction != null)
				return false;
		} else if (!varToRestriction.equals(other.varToRestriction))
			return false;
		return true;
	}
	*/
	
	
}



