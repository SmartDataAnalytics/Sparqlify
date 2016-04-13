package org.aksw.sparqlify.core.domain.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.sparqlify.algebra.sparql.transform.ConstantExpander;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;
import org.aksw.sparqlify.restriction.RestrictionSetImpl;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;

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
public class VarDefinition
//	implements Cloneable
{
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
	
	
	/**
	 * In place expression transformation
	 * 
	 * @param transform
	 */
	public void applyExprTransform(Function<Expr, Expr> transform) {
		
		Multimap<Var, RestrictedExpr> newVarToExprs = HashMultimap.create();
		for(Entry<Var, RestrictedExpr> entry : varToExprs.entries()) {
			
			Var var = entry.getKey();
			
			RestrictedExpr restExpr = entry.getValue();
			Expr expr = restExpr.getExpr();
			Expr ne = transform.apply(expr);
			
			RestrictionSetImpl r = restExpr.getRestrictions();
			RestrictedExpr nre = new RestrictedExpr(ne, r);
			//entry.setValue(new );
			newVarToExprs.put(var, nre);
			
		}
		
		varToExprs = newVarToExprs;
	}
	
	public VarDefinition copyProject(Collection<Var> viewVars) {
		Multimap<Var, RestrictedExpr> map = HashMultimap.create();
	
		for(Var var : viewVars) {
			Collection<RestrictedExpr> restExprs = varToExprs.get(var);
			
			map.putAll(var, restExprs);			
		}

		VarDefinition result = new VarDefinition(map);
		
		return result;
	}
	
	public VarDefinition copyExpandConstants() {
		Multimap<Var, RestrictedExpr> resultMap = HashMultimap.create();
		
		for(Entry<Var, RestrictedExpr> entry : varToExprs.entries()) {
			Var var = entry.getKey();
			
			RestrictedExpr restExpr = entry.getValue();
			Expr expr = restExpr.getExpr();
			
			//Expr expandedExpr = ConstantExpander.transform(expr);
			Expr expandedExpr;
			if(expr.isConstant()) {
				expandedExpr = ConstantExpander._transform(expr.getConstant());
			} else {
				expandedExpr = expr;
			}
			
			
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

	public VarDefinition copySubstitute(Map<Var, Expr> map) {
		VarDefinition result = VarDefinition.copySubstitute(this, map);
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

	//@Override
	public VarDefinition extend(VarDefinition that) {
		Multimap<Var, RestrictedExpr> map = HashMultimap.create(varToExprs);
		map.putAll(that.varToExprs);
		
		VarDefinition result = new VarDefinition(map);
		return result;
	}
	
	public String toPrettyString() {
		return toIndentedString(this);
	}
	
	public static String toIndentedString(VarDefinition varDef) {
		Multimap<Var, RestrictedExpr> map = varDef.getMap();
		String result = toIndentedString(map);
		return result;
	}
	
	
	public List<String> getReferencedNames() {
		Set<Var> tmp = getReferencedVars();
		List<String> result = new ArrayList<String>(tmp.size());
		for(Var var : tmp) {
			String name = var.getName();
			result.add(name);
		}
		
		return result;
	}
	
	public static <T extends Collection<String>> T copyVarNames(T target, Collection<Var> vars) {
		for(Var var : vars) {
			target.add(var.getName());
		}
		
		return target;
	}
	
	public Set<Var> getReferencedVars() {
		Set<Var> result = new HashSet<Var>();
		
		for(Entry<Var, RestrictedExpr> entry : varToExprs.entries()) {
			
			Set<Var> tmp = entry.getValue().getExpr().getVarsMentioned();
			result.addAll(tmp);			
		}
		
		return result;
	}
	
	public Multimap<Var, Expr> withoutRestrictions() {
		Multimap<Var, Expr> result = HashMultimap.create();
		for(Entry<Var, RestrictedExpr> entry : this.varToExprs.entries()) {
			result.put(entry.getKey(), entry.getValue().getExpr());
		}
		
		return result;
	}
	
	/**
	 * Return only the referenced vars for the given vars
	 * 
	 * @param vars
	 * @return
	 */
	public Set<Var> getReferencedVars(Collection<Var> vars) {
		//List<Var> result = new ArrayList<Var>();
		Set<Var> result = new HashSet<Var>();
		
		// Track all columns that contribute to the construction of SQL variables
		for(Var var : vars) {
			for(RestrictedExpr def : getDefinitions(var)) {
				Collection<Var> tmps = def.getExpr().getVarsMentioned(); 
				result.addAll(tmps);
				//for(Var item : def.getExpr().getVarsMentioned()) {
					
					//String itemName = item.getName();
					//if(!result.contains(item)) {
						//result.add(item);
					//}
				//}
			}
		}

		return result;
	}
	
	public Set<String> getReferencedVarNames(Collection<Var> vars) {
		Set<Var> tmp = getReferencedVars(vars);
		Set<String> result = copyVarNames(new HashSet<String>(), tmp);
		return result;
	}
	
	public <T extends Collection<String>> T getReferencedVarNames(T target, Collection<Var> vars) {
		Set<Var> tmp = getReferencedVars(vars);
		T result = copyVarNames(target, tmp);
		return result;
	}
	
	
	public static String toIndentedString(Multimap<Var, RestrictedExpr> varToExprs) {
		
		String result = "";
		
		for(Entry<Var, Collection<RestrictedExpr>> entry : varToExprs.asMap().entrySet()) {
			Var var = entry.getKey();
			String varName = var.getName();
			//int varLen = varName.length();
			Collection<RestrictedExpr> restExprs = entry.getValue();
			
			Iterator<RestrictedExpr> it = restExprs.iterator();
			
			String firstLabel;
			if(!it.hasNext()) {
				firstLabel = "(empty definition set)";
			} else {
				RestrictedExpr restExpr = it.next();
				firstLabel = varName + ": " + restExpr.getExpr() + " [" + restExpr.getRestrictions() + "]";
			}
			result += firstLabel + "\n";
			
			while(it.hasNext()) {
				RestrictedExpr restExpr = it.next();
				//StringUtils.
				// FIXME Make spaces for var length
				result += "    " + restExpr.getExpr() + " [" + restExpr.getRestrictions() + "]" + "\n";
			}
		}
		
		return result;
	}	
	
	@Override
	public String toString() {
		return toPrettyString();
		//return "VarDefinition [varToExprs=" + varToExprs + "]";
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
	
	public static VarDefinition create(VarExprList varExprs) {

		Multimap<Var, RestrictedExpr> map = HashMultimap.create();
		for(Entry<Var, Expr> entry : varExprs.getExprs().entrySet()) {
			Var var = entry.getKey();
			Expr expr = entry.getValue();
			
			RestrictedExpr restExpr = new RestrictedExpr(expr);
			
			map.put(var, restExpr);
		}
		
		VarDefinition result = new VarDefinition(map);
		return result;
	}
	
}
