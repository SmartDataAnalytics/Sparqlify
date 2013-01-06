package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class MappingRefactor {


	/*
	public static List<Mapping> refactorToUnion(List<Mapping> ms, Var var) {
		List<Mapping> result = new ArrayList<Mapping>();
		
		for(Mapping m : ms) {
			refactorToUnion(m, var, result);
		}
	
		return result;
	}
	
	public static List<Mapping> refactorToUnion(Mapping m, Var var, List<Mapping> result) {
		
		if(result == null) {
			result = new ArrayList<Mapping>();
		}
		
		
		VarDefinition varDef = m.getVarDefinition();

		Collection<RestrictedExpr> defs = varDef.getDefinitions(var);
			
		if(defs.size() > 1) {
			for(RestrictedExpr restExpr : defs) {
				
				Multimap<Var, RestrictedExpr> map = HashMultimap.create(varDef.getMap());
				map.removeAll(var);
				map.put(var, restExpr);
				
				VarDefinition newVd = new VarDefinition(map);
				Mapping newM = new Mapping(newVd, m.getSqlOp());
				
				result.add(newM);
			}
		} else {
			result.add(m);
		}

		return result;
	}
	*/

	
	/**
	 * Given a list of mappings, create groups of them with the following properties:
	 * All members of a group must have the same datatypes for their term constructor args
	 * e.g. rdfTerm(int, string, string, string), rdfTerm(int, float, string, string) 
	 * 
	 * 
	 */
	public static ListMultimap<String, Mapping> groupBy(ExprDatatypeNorm exprNormalizer, List<Mapping> ms, List<Var> vars) {
		
		//Multimap<String, ArgExpr> cluster = HashMultimap.create();
		ListMultimap<String, Mapping> cluster = LinkedListMultimap.create();
		
		for(Mapping m : ms) {

			Map<String, TypeToken> tmpTypeMap = m.getSqlOp().getSchema().getTypeMap();

			List<String> hashes = new ArrayList<String>(vars.size());
			for(Var var : vars) {

				Collection<RestrictedExpr> defs = m.getVarDefinition().getDefinitions(var);
				
				Expr expr;


				if(defs.size() > 1) {
					throw new RuntimeException("Encountered multiple variable definitions during group by. Var: " + var + ", Mapping: " + m);
				}
				else if(defs.isEmpty()) {

					// Example If only name is defined, but we request "GROUP BY name, age"
					// then this is the same as grouping by name only, since age is implicitely NULL

					// TODO Do we have to bind name to NULL?
					
					//expr = 
					//throw new RuntimeException("Cannot group by " + var + " because mapping does not define it. Mapping: " + m);
					expr = null;
				}
				else {
					RestrictedExpr restExpr = defs.iterator().next();
					expr = restExpr.getExpr();					
				}
				
				
				String hash;
				if(expr == null) {
					hash = "null";
				} else {
					Expr datatypeNorm = exprNormalizer.normalize(expr, tmpTypeMap);
					hash = datatypeNorm.toString();
				}
				
				hashes.add(hash);
			}
			
			String key = Joiner.on(",").join(hashes);
			
		
			cluster.put(key, m);
		}

		return cluster;
	}

	
	/**
	 * If there are multiple definitions for any of the specified variables,
	 * each definition results in a new mapping.
	 * The result is then a union of these mappings.
	 * 
	 * @param m
	 */
	public static List<Mapping> refactorToUnion(Mapping m, List<Var> tmpVars) {
		
		List<Mapping> result = new ArrayList<Mapping>();
		
		VarDefinition varDef = m.getVarDefinition();
		
		List<Var> vars = new ArrayList<Var>(tmpVars.size());
		List<Collection<RestrictedExpr>> c = new ArrayList<Collection<RestrictedExpr>>(tmpVars.size());

		
		if(tmpVars.isEmpty()) {
			result.add(m);
			return result;
		}
		
		for(Var var : tmpVars) {
			
			Collection<RestrictedExpr> defs = varDef.getDefinitions(var);
			if(defs.isEmpty()) {
				continue;
			}
			
			vars.add(var);
			c.add(defs);
		}

		Multimap<Var, RestrictedExpr> baseMap = HashMultimap.create(varDef.getMap());		
		baseMap.keySet().removeAll(vars);

		CartesianProduct<RestrictedExpr> cart = CartesianProduct.create(c);

		
		for(List<RestrictedExpr> item : cart) {
			
	
			Multimap<Var, RestrictedExpr> map = HashMultimap.create(varDef.getMap());
			for(int i = 0; i < vars.size(); ++i) {
				Var var = vars.get(i);
				RestrictedExpr restExpr = item.get(i);
				
				map.put(var, restExpr);
			}
			
			VarDefinition newVd = new VarDefinition(map);
			Mapping newM = new Mapping(newVd, m.getSqlOp());
			
			result.add(newM);
		}
		

		/*
		OpDisjunction result = null;
		return result;
		*/
		return result;
	}
}
