package org.aksw.sparqlify.core.domain;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

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
	private Multimap<Var, RestrictedExpr<Expr>> varToExprs;
	
	
	public VarDefinition() {
		this.varToExprs = HashMultimap.create();
	}
	
	public Multimap<Var, RestrictedExpr<Expr>> getMap() {
		return varToExprs;
	}
	
	//public Collection<RestrictedExpr<Expr>> get(Var q)
	
	public Collection<RestrictedExpr<Expr>> getDefinitions(Var viewVar) {
		return varToExprs.get(viewVar);
	}
	
	
	/**
	 * Traverses a SPARQL expressions, and moves into any
	 * the SqlExpr-arguments of any E_RdfTerm it encounters.
	 * 
	 * The column references are then replaced inside the SqlExprs.
	 *  
	 * 
	 * @param expr
	 * @return
	 */
	/*
	public static Expr renameColumnReferences(Expr expr) {
	}
	
	public static SqlExpr renameColumnReferences(SqlExpr sqlExpr, Map<String, String> oldToNew) {
		
		
		
		ColumnSubstitutor columnRenamer = new ColumnSubstitutor(map)
		SqlExpr result = columnRenamer._transform(map);
		
		return result;
	}
	
	
		
	
	public createWithRenamedColumnReferences(Map<String, String> map) {
		
		
		// Substitute the column references in b
		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(exprMap);

		Multimap<Var, VarDef> newSparqlMap = HashMultimap.create();
		for(Entry<Var, VarDef> entry : right.getSparqlVarToExprs().entries()) {
			VarDef before = entry.getValue();
			VarDef after = new VarDef(substitutor.transformMM(before.getExpr()), before.getRestrictions());
			
			newSparqlMap.put(entry.getKey(), after);
		}
		//b.getSparqlVarToExprs().clear();
		//b.getSparqlVarToExprs().putAll(newSparqlMap);
		result.getSparqlVarToExprs().putAll(newSparqlMap);
		
		return result;
	}
	*/
	
	
	/*
	public renameColumnReferences(Map<String, String> oldToNew) {
	
		for(Entry<Var, VariableDefinition> mapExpr : varToMapExprs.entrySet()) {
			if(mapExpr.)
		}
		
		
	}*/
}
