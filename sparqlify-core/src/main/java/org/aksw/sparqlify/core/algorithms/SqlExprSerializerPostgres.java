package org.aksw.sparqlify.core.algorithms;

import java.util.Calendar;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.core.SqlDatatype;
import org.postgis.PGgeometry;

import com.hp.hpl.jena.sdb.sql.SQLUtils;

interface DatatypeToString
{
	public Factory1<String> asString(SqlDatatype datatype);
}


public class SqlExprSerializerPostgres
extends SqlExprSerializerDefault
{

public SqlExprSerializerPostgres(DatatypeAssigner datatypeAssigner) {
	super(datatypeAssigner, new DatatypeToStringPostgres());
}




public String serializeConstant(Object value, SqlDatatype datatype) {
	if(value == null) {
		//String cast = "::" + datatypeSerializer.asString(datatype);
		
		Factory1<String> caster = datatypeSerializer.asString(datatype);
		
		return caster.create("NULL");
	} else if(value instanceof String) {
		return SQLUtils.quoteStr(value.toString()); 
	} else if(value instanceof Number) {
		return value.toString();
	} else if(value instanceof Calendar) {
		java.sql.Timestamp sqlDateTime = new java.sql.Timestamp(((Calendar)value).getTime().getTime());
		return SQLUtils.quoteStr(sqlDateTime.toString());
	} else if (value instanceof Boolean) {
		return value.toString();
	} else if (value instanceof PGgeometry) {
		//return "'SRID=4326;" + value.toString() + "'::geometry";
		return "'SRID=4326;" + value.toString() + "'";
	} else {
		throw new RuntimeException("Don't know how to serialize " + value + " to an SQL string");
	}
}



}

/*
class DatatypeToStringMySql
	implements DatatypeToString
{
	public String asString(SqlDatatype datatype)
	{
		String result = (String)MultiMethod.invoke(this, "_asString", datatype);
		return result;		
	}

	public String _asString(SqlDatatypeDateTime datatype) {
		return "datetime";
	}

	public String _asString(SqlDatatypeReal datatype) {
		return "decimal";
	}

	public String _asString(SqlDatatypeInteger datatype) {
		return "decimal";
	}
	
	public String _asString(SqlDatatypeString datatype) {
		return "char";
	}
	
	public String _asString(SqlDatatypeBigInteger datatype) {
		return "decimal";
	}	

}*/






/*
class ExprSerializerMySql
	extends ExprSerializerDefault
{

	public ExprSerializerMySql() {
		super(new DatatypeToStringMySql());
	}
	
	public String serializeConstant(Object value, SqlDatatype datatype) {

		if(value == null) {			
			return "CAST(NULL AS " + datatypeSerializer.asString(datatype) + ")";
		} else if(value instanceof String) {
			return SQLUtils.quoteStr(value.toString()); 
		} else {
			return value.toString();
		}		
	}

}
*/