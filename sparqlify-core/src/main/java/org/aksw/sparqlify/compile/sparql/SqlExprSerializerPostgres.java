package org.aksw.sparqlify.compile.sparql;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.S_Arithmetic;
import org.aksw.sparqlify.algebra.sql.exprs.S_Cast;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeographyFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeometryFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_Intersects;
import org.aksw.sparqlify.algebra.sql.exprs.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.S_String;
import org.aksw.sparqlify.algebra.sql.exprs.SqlAggregator;
import org.aksw.sparqlify.algebra.sql.exprs.SqlAggregatorCount;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.algebra.sql.exprs.SqlStringTransformer;
import org.aksw.sparqlify.core.SqlDatatype;
import org.apache.jena.sdb.sql.SQLUtils;
import org.postgis.PGgeometry;

import com.google.common.base.Joiner;

interface DatatypeToString
{
	public Factory1<String> asString(SqlDatatype datatype);
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



class DatatypeToStringPostgres
	implements DatatypeToString
{
	private Map<String, String> nameToPostgres = new HashMap<String, String>();

	public DatatypeToStringPostgres() {
		// TODO: Use the datatype system map for reverse mapping
		nameToPostgres.put("boolean", "boolean");
		nameToPostgres.put("float", "double precision");
		nameToPostgres.put("double", "double precision");
		nameToPostgres.put("integer", "integer");
		nameToPostgres.put("string", "text");
		nameToPostgres.put("geometry", "geometry");
		nameToPostgres.put("geography", "geography");
		nameToPostgres.put("int", "integer");
		nameToPostgres.put("long", "bigint");
		
		//nameToPostgres.put("d", "datetime");
		nameToPostgres.put("datetime", "date");
		// bigint nameToPostgres.put("geography", "geography");
	}
	
	/**
	 * Performs a type cast
	 */
	public Factory1<String> asString(SqlDatatype datatype)
	{
		/*
		if(datatype.getName().equals("geography")) {
			return new Factory1<String>() {

				@Override
				public String create(String a) {
					return "ST_AsText(" + a +")";
				}
				
			};
		}*/

		final String result = nameToPostgres.get(datatype.getName());
		if(result == null) {
			throw new RuntimeException("No string representation for " + datatype.getName());
		}
		//return result;

		
		return new Factory1<String>() {

			@Override
			public String create(String a) {
				return a + "::" + result;
			}
		};

		//String result = (String)MultiMethod.invoke(this, "_asString", datatype);
		//return result;		
	}
	
	
	/**
	 * Uses custom function to convert between types
	 * (e.g ST_AsText(geometry) for conversion to string
	 * 
	 * @param datatype
	 * @return
	 */
	public Factory1<String> formatString(SqlDatatype datatype)
	{
		if(datatype.getName().equals("geometry") || datatype.getName().equals("geography")) {
			return new Factory1<String>() {

				@Override
				public String create(String a) {
					return "ST_AsText(" + a +")";
				}
				
			};
		}

		final String result = nameToPostgres.get(datatype.getName());
		if(result == null) {
			throw new RuntimeException("No string representation for " + datatype.getName());
		}
		//return result;

		
		return new Factory1<String>() {

			@Override
			public String create(String a) {
				return a + "::" + result;
			}
		};

		//String result = (String)MultiMethod.invoke(this, "_asString", datatype);
		//return result;		
	}

}


/*
class SqlExprSerializerMySql
	extends SqlExprSerializerDefault
{

	public SqlExprSerializerMySql() {
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

class SqlExprSerializerPostgres
	extends SqlExprSerializerDefault
{

	public SqlExprSerializerPostgres() {
		super(new DatatypeToStringPostgres());
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


abstract class SqlExprSerializerDefault
	implements SqlExprSerializer
{

	protected DatatypeToString datatypeSerializer;
	
	public SqlExprSerializerDefault(DatatypeToString datatypeSerializer)
	{
		this.datatypeSerializer = datatypeSerializer;
	}
	
	
	
	
	public String serialize(SqlExpr expr) {
		try {
			String result = (String)MultiMethod.invoke(this, "_serialize", expr);
		
			return result;
		} catch(Exception e) {
			throw new RuntimeException("Error serializing expression" + expr, e);
		}
	}
	
	
	public String _serialize(SqlExprAggregator expr) {
		String result = this._serializeAgg(expr.getAggregator());
		return result;
	}
	
	
	public String _serializeAgg(SqlAggregator aggregator) {
		String result = MultiMethod.invoke(this, "serializeAgg", aggregator);
		return result;
	}
	
	public String serializeAgg(SqlAggregatorCount aggregator) {
		return "COUNT(*)";
	}
	
	
	
	
		
	public List<String> serializeArgs(List<SqlExpr> exprs) {
		List<String> result = new ArrayList<String>();
		for(SqlExpr expr : exprs) {
			String tmp = serialize(expr);
			result.add(tmp);
		}
		
		return result;
	}

	public String _serialize(S_Cast expr) {		
		
		String tmp = serialize(expr.getExpr());
		Factory1<String> caster = datatypeSerializer.asString(expr.getDatatype());

		String result = caster.create(tmp);
		
		return result;
	}
	
	public String _serialize(S_Function expr) {
		List<String> args = serializeArgs(expr.getArgs());
		
		SqlStringTransformer transformer = expr.getTransformer();
		
		if(transformer == null) {
			return expr.getFuncName() + "(" + Joiner.on(", ").join(serializeArgs(expr.getArgs())) + ")";
		} else {
			return transformer.transform(expr, args);
		}
	}

	/*
	public static String _serialize(SqlExpr expr) {
		return expr.asSQL();
	}
	*/
	public String _serialize(SqlExprColumn expr) 
	{
		String columnAlias = SqlAlgebraToString.escapeAlias(expr.getColumnName()); 
		
		if(expr.getTableName() == null) {
			return columnAlias;
		} else {
			return expr.getTableName() + "." + columnAlias;
		}

		
		//return expr.asString();
	}

	
		
	/*
	public static String _serialize(SqlConstant expr) {
		return serializeConstant(expr.getValue(), expr.getDatatype());
	}*/
	
	
	public abstract String serializeConstant(Object value, SqlDatatype datatype);

	
	public String _serialize(S_String expr) {
		return expr.getSqlString();
	}

	// unused
	public String _serialize(SqlExprValue expr) {
		return serializeConstant(expr.getObject(), expr.getDatatype());
	}

	public String _serialize(S_GeographyFromText expr)
	{
		return "ST_GeographyFromText(" + serialize(expr.getExpr()) + ")"; 
	}

	public String _serialize(S_GeometryFromText expr)
	{
		return "ST_GeomFromText(" + serialize(expr.getExpr()) + ", 4326)"; 
	}
	
	public String _serialize(S_Intersects expr)
	{
		// Did I have some argument against ST_Intersects???
		//return "(" + serialize(expr.getLeft()) + " && " + serialize(expr.getRight()) + ")";
		
		
		return "ST_Intersects(" + serialize(expr.getLeft()) + ", " + serialize(expr.getRight()) + ")"; 
	}
	
	
	public String _serialize(S_LessThan expr) {
		return "(" + serialize(expr.getLeft()) + " < " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_LessThanOrEqual expr) {
		return "(" + serialize(expr.getLeft()) + " <= " + serialize(expr.getRight()) + ")";
	}
	
	public String _serialize(S_Equals expr) {
		return "(" + serialize(expr.getLeft()) + " = " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_GreaterThanOrEqual expr) {
		return "(" + serialize(expr.getLeft()) + " >= " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_GreaterThan expr) {
		return "(" + serialize(expr.getLeft()) + " > " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_LogicalAnd expr) {
		return "(" + serialize(expr.getLeft()) + " AND " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_LogicalOr expr) {
		return "(" + serialize(expr.getLeft()) + " OR " + serialize(expr.getRight()) + ")";
	}

	public String _serialize(S_LogicalNot expr) {
		return "(NOT " + serialize(expr.getExpr()) + ")";
	}

	public String _serialize(S_Arithmetic expr) {
		return "(" + serialize(expr.getLeft()) + " " + expr.getSymbol() + " " + serialize(expr.getRight()) + ")";
	}
	
	public String _serialize(S_IsNotNull expr) {
		String arg = serialize(expr.getExpr());
		String result = "(" + arg + " IS NOT NULL)";
		
		return result;
	}
	
	public String _serialize(S_Concat concat) {
		
		List<String> args = serializeArgs(concat.getArgs());
		String result = "(" + Joiner.on(" || ").join(args) + ")";

		return result;
	}
	
	public String _serialize(S_Regex expr) {
		
		String arg = serialize(expr.getExpr());
		String result = arg + " ~* " + "'" + expr.getPattern() + "'";

		return result;
	}
	
}