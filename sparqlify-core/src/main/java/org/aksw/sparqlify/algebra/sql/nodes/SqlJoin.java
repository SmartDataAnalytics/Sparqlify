package org.aksw.sparqlify.algebra.sql.nodes;


import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;

import com.hp.hpl.jena.sdb.core.JoinType;



public class SqlJoin
	extends SqlNodeBase2
{
	private JoinType joinType;
	private SqlExprList conditions = new SqlExprList();

	///* TODO Maybe we should assign alias at joins rather than nodes?

	
	// NOTE For my future self: There is no point in using aliases in the join
	// A select statement looks like Select [projection] From join-expression
	// Where each element of the join-expression must provide its own
	// alias, such as RdfViewInstance(myTable) -> Select * From myTable As a
	// Filter(RdfViewInstace(myTable) -> ???
	/*
	private String leftAlias;
	private String rightAlias;
	
	public String getLeftAlias() {
		return leftAlias;
	}
	
	public String getRightAlias() {
		return rightAlias;
	}
	*/

	/*
	public static SqlJoin create(JoinType joinType, SqlNode left, String leftAlias, SqlNode right, String rightAlias) {
		return new SqlJoin(null, joinType, left, leftAlias, right, rightAlias);
	}*/



	public static SqlJoin create(JoinType joinType, SqlNodeOld left, SqlNodeOld right) {
		return new SqlJoin(null, joinType, left, right);
	}
	
	public SqlJoin(String aliasName, JoinType joinType, SqlNodeOld left, SqlNodeOld right) {
		super(aliasName, left, right);
		this.joinType = joinType;
	}
	

	/*
	public SqlJoin(String aliasName, JoinType joinType, SqlNode left, String leftAlias, SqlNode right, String rightAlias) {
		super(aliasName, left, right);
		this.joinType = joinType;
		this.leftAlias = leftAlias;
		this.rightAlias = rightAlias;
	}*/

	
	public void addCondition(SqlExpr expr) {
		this.getConditions().add(expr);
	}
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public SqlExprList getConditions() {
		return conditions;
	}

	@Override
	SqlJoin copy2(SqlNodeOld left, SqlNodeOld right) {
		// TODO Auto-generated method stub
		return null;
	}
}
