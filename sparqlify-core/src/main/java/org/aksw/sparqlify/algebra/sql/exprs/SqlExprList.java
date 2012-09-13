package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SqlExprList
	extends AbstractList<SqlExpr>
{
	private List<SqlExpr> exprs;
	
	public SqlExprList()
	{
		exprs = new ArrayList<SqlExpr>();
	}

	public SqlExprList(SqlExpr sqlExpr)
	{
		exprs = new ArrayList<SqlExpr>();
		exprs.add(sqlExpr);
	}

	public List<SqlExpr> asList()
	{
		return exprs;
	}
	
	@Override
	public boolean add(SqlExpr item)
	{
		return exprs.add(item);
	}

	@Override
	public SqlExpr get(int index) {
		return exprs.get(index);
	}

	@Override
	public Iterator<SqlExpr> iterator() {
		return exprs.iterator();
	}

	@Override
	public int size() {
		return exprs.size();
	}
}
