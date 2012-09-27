package org.aksw.sparqlify.core.domain;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

import com.hp.hpl.jena.sparql.core.Var;


public class VarReference {
	private String targetViewName;
	private Var targetVar;
	
	private SqlExpr joinCondition;

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
		this.joinCondition = joinCondition;
	}

	public String getTargetViewName() {
		return targetViewName;
	}

	public Var getTargetVar() {
		return targetVar;
	}

	public SqlExpr getJoinCondition() {
		return joinCondition;
	}
}
