package org.aksw.sparqlify.algebra.sql.nodes;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.views.VarDef;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.core.ConstraintContainer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/*
 class SqlDatatypeSignature {
 Map<SqlDatatype, Integer> datatypeToCount = new HashMap<SqlDatatype, Integer>();

 public void()
 }*/

public abstract class SqlNodeBase implements SqlNodeOld {
	protected String aliasName;
	private Multimap<Var, VarDef> sparqlVarToExpr = HashMultimap.create();

	private Map<String, SqlExpr> aliasToColumn = new HashMap<String, SqlExpr>();

	
	//private List<Node> order = null;
	
	private ConstraintContainer constraints;
	
	
	public ConstraintContainer getConstraints()
	{
		return constraints;
	}
	
	
	/*
	public List<Node> getOrder() 
	{
		return order;
	}
	
	public void setOrder(List<Node> order)
	{
		this.order = order;
	}*/
	
	
	/**
	 * If an order has been set, it is returned, otherwise returns the variables in the map
	 * 
	 * @return
	 */
	/*
	public List<Node> getInferredOrder()
	{
		return order != null ? order : new ArrayList<Node>(sparqlVarToExpr.keySet());
	}
	*/
	
	// Helper columns - these variables become aliases for
	// expressions on the underlying columns
	// The helper columns can be referred to by the sparqlVar expressions.
	// private Map<Var, Expr> sqlVarToExpr = new HashMap<Var, Expr>();

	public Map<String, SqlExpr> getAliasToColumn() {
		return aliasToColumn;
	}

	public SqlNodeBase(String aliasName) {
		this.aliasName = aliasName;
	}

	public Multimap<Var, VarDef> getSparqlVarToExprs() {
		return sparqlVarToExpr;
	}

	public Set<Var> getSparqlVarsMentioned() {
		return sparqlVarToExpr.keySet();
	}

	/*
	 * public Multimap<List<SqlDatatype>, Expr> getDatatypesToExprs() { for(Expr
	 * expr : sparqlVarToExpr.values()) { expr.getVarsMentioned();
	 * 
	 * }
	 * 
	 * }
	 */

	/*
	 * public Map<Var, Expr> getSqlVarToExpr() { return sqlVarToExpr; }
	 */

	@Override
	public String getAliasName() {
		return aliasName;
	}


	
	@Override
	public String toString() 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(out);
		write(writer);
		return out.toString();
	}

	@Override
	public void write(IndentedWriter writer) 
	{
		writer.println( "(" + this.getClass().getSimpleName());
		writer.incIndent();
		for(SqlNodeOld arg : this.getArgs()) {
			arg.write(writer);
		}
		writer.decIndent();
		writer.println(") " + this.getAliasName());
	}
	
	
	// abstract boolean checkValidArgsForCopy(SqlNode[] args);
}
