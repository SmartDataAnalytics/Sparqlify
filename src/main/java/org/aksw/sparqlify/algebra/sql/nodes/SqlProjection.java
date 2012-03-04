package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;


/**
 * Projects the columns of the inner statement to new colums.
 * Can be used for renaming variables and performing calculations on them.
 * 
 * FIXME Note sure if expression should be of type Expr or SqlExpr.
 * Currently I use Expr for everything in order not having to implement
 * transformations twice.
 * 
 * TODO I think this class should be superseded by SqlSelectBlock(?)
 * 
 */
public class SqlProjection
	extends SqlNodeBase1
{
	//public static final String name = "SqlProjection";	
	//private Map<Var, Expr> aliasToExpr = new HashMap<Var, Expr>();
	private List<Var> order = new ArrayList<Var>(); 
	private Map<Var, Expr> projection = new HashMap<Var, Expr>();

	public SqlProjection(String aliasName, SqlNode sqlNode) {
		super(aliasName, sqlNode);
	}

	public SqlProjection(String aliasName, SqlNode sqlNode, List<Var> order, Map<Var, Expr> aliasToExpr) {
		super(aliasName, sqlNode);
		this.order = order;
		this.projection = aliasToExpr;
	}
	
	/*
	public List<Var> getOrder() {
		return order;
	}*/
	
	public Map<Var, Expr> getProjection() {
		return projection;
	}


    @Override
    SqlNode copy1(SqlNode subNode)
    {
        // TODO May need to do a deeper copy.
    	SqlProjection s = new SqlProjection(this.getAliasName(), subNode, this.order, this.projection) ;
        //s.idScope = this.idScope ;
        //s.nodeScope = this.nodeScope ;
        return s ;
    }

	
	/*
	public OpProjection(Map<Expr, Var> exprToAlias)
	{
		super(name);
		// TODO Auto-generated constructor stub
	}*/
	

	/*
	@Override
	public Op effectiveOp()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
	{
		// TODO Auto-generated method stub
		return false;
	}
	*/
}
