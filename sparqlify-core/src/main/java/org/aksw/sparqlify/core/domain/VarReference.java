package org.aksw.sparqlify.core.domain;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

import com.hp.hpl.jena.sparql.core.Var;


/**
 * TODO This is a syntactic construct that gets translated into something different (i think)
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class VarReference {
	private String targetViewName;
	private Var targetVar;
	
	/**
	 * 
	 * 
	 * @param targetViewName
	 * @param targetVar
	 * @param joinCondition A pure SQL join condition (completely unrelated to the SPARQL level),
	 *        however with this/that placeholder (this.person_id = that.person_id)
	 */
	public VarReference(String targetViewName, Var targetVar, SqlExpr joinCondition) {
		super();
		this.targetViewName = targetViewName;
		this.targetVar = targetVar;
	}

	public String getTargetViewName() {
		return targetViewName;
	}

	public Var getTargetVar() {
		return targetVar;
	}
}
