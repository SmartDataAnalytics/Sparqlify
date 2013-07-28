package org.aksw.sparqlify.algebra.sql.exprs;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.SqlDatatype;
import org.apache.jena.atlas.io.IndentedWriter;

public abstract class SqlExprBase
	implements SqlExpr
{	
	private SqlDatatype datatype;

	public SqlExprBase(SqlDatatype datatype) {
		this.datatype = datatype;
		
		if(datatype == null) {
			throw new NullPointerException();
		}
	}

	public SqlDatatype getDatatype() {
		return datatype;
	}

	/*
	public void setDatatype(SqlDatatype datatype) {
		this.datatype = datatype;
	}
	*/
	
	@Override
	public String toString() 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(out);
		asString(writer);
		return out.toString();
	}

	@Override
	public void asString(IndentedWriter writer) 
	{
		asString(writer, this.getClass().getSimpleName(), getArgs());
	}
	
	public static void asString(IndentedWriter writer, String nodeName, SqlExpr ... args) {
		asString(writer, nodeName, Arrays.asList(args));
	}
	
	public static void asString(IndentedWriter writer, String nodeName, List<SqlExpr> args) {
		writer.println( "(" + nodeName);
		writer.incIndent();
		for(SqlExpr arg : args) {
			arg.asString(writer);
		}
		writer.decIndent();
		writer.println(")");
	}

	public static Set<SqlExprColumn> getColumnsMentioned(SqlExpr expr) {
		Set<SqlExprColumn> result = new HashSet<SqlExprColumn>();
		
		_getColumnsMentioned(result, expr);
		
		return result;
	}
	
	public static void _getColumnsMentioned(Set<SqlExprColumn> result, SqlExpr expr) {
		if(expr instanceof SqlExprColumn) {
			result.add((SqlExprColumn)expr);
			return;
		}
		
		for(SqlExpr arg : expr.getArgs()) {
			_getColumnsMentioned(result, arg);
		}
	}
}
