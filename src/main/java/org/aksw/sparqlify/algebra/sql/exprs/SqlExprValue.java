package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatypeEvaluator;


public class SqlExprValue
	extends SqlExpr0
{
	public static final SqlExprValue FALSE = new SqlExprValue(false);
	public static final SqlExprValue TRUE = new SqlExprValue(true);

	
	private Object object;
	
	public SqlExprValue(Object object)
	{
		super(SqlDatatypeEvaluator.getDatatype(object));
		this.object = object;
	}
	
	public SqlExprValue(Object object, SqlDatatype datatype) {
		super(datatype);
		this.object = object;
	}

	
	public static SqlExprValue createNull(SqlDatatype datatype) {
		return new SqlExprValue(null, datatype);
	}

	
	public Object getObject() {
		return object;
	}
	
	@Override
	public String toString() {
		return "" + object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		SqlExprValue other = (SqlExprValue) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}
}
