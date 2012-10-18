package org.aksw.sparqlify.algebra.sql.exprs2;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public abstract class SqlExprBase
	implements SqlExpr
{	
	private TypeToken datatype;

	public SqlExprBase(TypeToken datatype) {
		this.datatype = datatype;
		
		if(datatype == null) {
			throw new NullPointerException();
		}
	}

	
	public boolean isVariable() {
		return SqlExprType.Variable.equals(getType());
	}
	
	
	public boolean isFunction() {
		return SqlExprType.Function.equals(getType());		
	}
	
	public SqlExprFunction asFunction() {
		return (SqlExprFunction)this;
	}

	public SqlExprConstant asConstant() {
		return (SqlExprConstant)this;
	}
	
	public SqlExprVar asVariable() {
		return (SqlExprVar)this;
	}

	public boolean isConstant() {
		return SqlExprType.Constant.equals(getType());				
	}
	
	public TypeToken getDatatype() {
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
		writer.flush();
		writer.close();
		return out.toString();
	}
/*
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
	*/

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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((datatype == null) ? 0 : datatype.hashCode());
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
		SqlExprBase other = (SqlExprBase) obj;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		} else if (!datatype.equals(other.datatype))
			return false;
		return true;
	}

}
