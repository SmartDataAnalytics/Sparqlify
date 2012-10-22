package org.aksw.sparqlify.core.domain.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sparql.transform.ConstantExpander;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;

/**
 * A variable definition binds a set of SPARQL variables to a
 * set of corresponding defining expressions
 * 
 * A variable definition consists of a
 * - a restricted expression that defines the variable
 * - an optional set of restriction expressions that restricts the variables set of values //apply to the variable under this definition
 * 
 * The expression can either be:
 * - a constant NodeValue that directly associates the variable with a constant
 * - an expression of type E_RdfTermCtor,
 * 
 * - other expression types probably do not make sense here - at least I don't see use cases for them (yet). 
 * 
 * @author Claus Stader
 *
 */
public class VarDefinition {
	private Multimap<Var, RestrictedExpr> varToExprs;
	
	
	public VarDefinition() {
		this.varToExprs = HashMultimap.create();
	}

	public VarDefinition(Multimap<Var, RestrictedExpr> varToExprs) {
		this.varToExprs = varToExprs;
	}

	
	boolean isEmpty() {
		return varToExprs.isEmpty();
	}
	
	public Multimap<Var, RestrictedExpr> getMap() {
		return varToExprs;
	}
	
	
	//public Collection<RestrictedExpr<Expr>> get(Var q)
	
	public Collection<RestrictedExpr> getDefinitions(Var viewVar) {
		return varToExprs.get(viewVar);
	}
	
	
	public VarDefinition copyExpandConstants() {
		Multimap<Var, RestrictedExpr> resultMap = HashMultimap.create();
		
		for(Entry<Var, RestrictedExpr> entry : varToExprs.entries()) {
			Var var = entry.getKey();
			
			RestrictedExpr restExpr = entry.getValue();
			Expr expr = restExpr.getExpr();
			
			Expr expandedExpr = ConstantExpander.transform(expr);
			RestrictedExpr finalExpr = new RestrictedExpr(expandedExpr, restExpr.getRestrictions());
			
			resultMap.put(var, finalExpr);
		}
		
		VarDefinition result = new VarDefinition(resultMap);
		return result;
	}
	
	
	public VarDefinition copyRenameVars(Map<Var, Var> oldToNew) {
		Multimap<Var, RestrictedExpr> resultMap = HashMultimap.create();
		
		for(Entry<Var, Collection<RestrictedExpr>> entry : varToExprs.asMap().entrySet()) {
			Var var = entry.getKey();
			Var renamedVar = oldToNew.get(var);
			
			Var newVar = renamedVar == null ? var : renamedVar;
			
			resultMap.putAll(newVar, entry.getValue());
		}
		
		VarDefinition result = new VarDefinition(resultMap);
		return result;
	}
	
	// Some Ideas for compact syntax:
	// Construct {?s rdfs:label ?name} With ?s = uri(@rdf, id) From table
	// Omitting the With clause alltogether: Construct { uri(@rdf, id) rdfs:label ?name } From table --- name will become a typed literal of type string.
	
	// FIXME This method should not be static but a real member
	public static VarDefinition copyRename(VarDefinition varDef, Map<String, String> oldToNew) {
		Map<Var, Expr> map = new HashMap<Var, Expr>();
		
		for(Entry<String, String> entry : oldToNew.entrySet()) {
			map.put(Var.alloc(entry.getKey()), new ExprVar(Var.alloc(entry.getValue())));
		}
		
		VarDefinition result = copySubstitute(varDef, map);
		
		return result;
	}
	
	public static VarDefinition copySubstitute(VarDefinition varDef, Map<Var, Expr> map) {
		
		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(map);

		
		Multimap<Var, RestrictedExpr> newVarToExpr = HashMultimap.create();
		for(Entry<Var, RestrictedExpr> entry : varDef.getMap().entries()) {
			RestrictedExpr before = entry.getValue();
			
			Expr newExpr = substitutor.transformMM(before.getExpr());
			
			RestrictedExpr after = new RestrictedExpr(newExpr, before.getRestrictions());
			
			newVarToExpr.put(entry.getKey(), after);
		}
		
		VarDefinition result = new VarDefinition(newVarToExpr);
		
		return result;
	}

	@Override
	public String toString() {
		return "VarDefinition [varToExprs=" + varToExprs + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((varToExprs == null) ? 0 : varToExprs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarDefinition other = (VarDefinition) obj;
		if (varToExprs == null) {
			if (other.varToExprs != null)
				return false;
		} else if (!varToExprs.equals(other.varToExprs))
			return false;
		return true;
	}
	
	
	/*
	public renameColumnReferences(Map<String, String> oldToNew) {
	
		for(Entry<Var, VariableDefinition> mapExpr : varToMapExprs.entrySet()) {
			if(mapExpr.)
		}
		
		
	}*/
	
	
	
}
