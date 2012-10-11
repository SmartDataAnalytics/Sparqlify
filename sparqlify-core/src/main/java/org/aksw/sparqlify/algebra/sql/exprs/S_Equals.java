package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.Set;

import org.aksw.commons.util.Pair;
import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.SqlDatatype;

public class S_Equals
	extends SqlExpr2
{

	public S_Equals(SqlExpr left, SqlExpr right) {
		super(left, right, DatatypeSystemDefault._BOOLEAN);// SqlDatatypeBoolean.getInstance());
	}
	
	@Override
	public String toString() {
		return "(" + getLeft() + " = " + getRight() + ")";
	}
	
		
	public static SqlDatatype getCommonDataype(SqlExpr left, SqlExpr right, DatatypeSystemOld system) {
		Set<SqlDatatype> commons = system.supremumDatatypes(left.getDatatype(), right.getDatatype());

		// TODO We should probably return type error here
		if(commons.isEmpty()) {
			return null;
		}

		if(commons.size() > 1) {
			throw new RuntimeException("Ambiguous type candidates: " + commons);
		}
		
		return commons.iterator().next();
	}
	
	public static SqlExprValue asConstant(SqlExpr expr) {
		return (expr instanceof SqlExprValue)
				? (SqlExprValue)expr
				: null;
	}
	
	public static SqlExprColumn asColumn(SqlExpr expr) {
		return (expr instanceof SqlExprColumn)
				? (SqlExprColumn)expr
				: null;		
	}
	
	public static SqlExprValue tryCast(SqlExprValue value, SqlDatatype datatype) {
		return value;
	}
	
	public static Pair<? extends SqlExpr, ? extends SqlExpr> resolveCast(SqlExpr left, SqlExpr right, DatatypeSystemOld system) {
		Pair<SqlExprColumn, SqlExprValue> pair = tryMatch(left, right);
		if(pair == null) {
			return Pair.create(left, right);
		}

		try {
		
		if(pair.getKey().getDatatype().equals(pair.getValue().getDatatype())) {
			return pair;
		}
		
		} catch(Throwable t) {
			System.out.println("ffs");
		}
		
		
		Object value = pair.getValue().getObject();

		Object castedValue = system.cast(value, pair.getKey().getDatatype());
		if(castedValue == null) {
			return null;
		}
		
		
		return Pair.create(pair.getKey(), new SqlExprValue(castedValue));
		
	}
	
	public static Pair<SqlExprColumn, SqlExprValue> tryMatch(SqlExpr left, SqlExpr right) {
		Pair<SqlExprColumn, SqlExprValue> result = tryMatchDirected(left, right);
		if(result == null) {
			result = tryMatchDirected(right, left);
		}
		
		return result;
	}
	
	public static Pair<SqlExprColumn, SqlExprValue> tryMatchDirected(SqlExpr left, SqlExpr right) {
		SqlExprColumn column = asColumn(left);
		if(column == null) {
			return null;
		}
		
		SqlExprValue value = asConstant(right);
		if(value == null) {
			return null;
		}
		
		return Pair.create(column, value);
	}
	
	
	
	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystemOld system) {
		
		// TODO Should we allow conversions, such as in '?var::int = 123456::string?
		
		// If one var is a constant, and the other is a variable, and the types differ,
		// try casting the constant

		
		
		
		
		// TODO We should probably return type error here
		if(getCommonDataype(left, right, system) == null) {
			
			
			Pair<? extends SqlExpr, ? extends SqlExpr> pair = resolveCast(left, right, system);
			if(pair == null) {
				return SqlExprValue.FALSE;
			}
			
			if(getCommonDataype(pair.getKey(), pair.getValue(), system) == null) {
				return SqlExprValue.FALSE;
			} else {
				return new S_Equals(pair.getKey(), pair.getValue());
			}
			
		}
		
		//SqlDatatype common = commons.iterator().next(); 
		
		/*
		// TODO A quick hack for testing
		if(left.getDatatype() instanceof SqlDatatypeGeography && right.getDatatype() instanceof SqlDatatypeString)
			return SqlExprValue.FALSE;
		
		if(right.getDatatype() instanceof SqlDatatypeGeography && left.getDatatype() instanceof SqlDatatypeString)
			return SqlExprValue.FALSE;
		*/
		
		return new S_Equals(left, right);
	}

	
	
/*
	public SqlExpr eval(DatatypeSystem system) {
		
		
		// TODO We should probably return type error here
		SqlExpr result;
		
		if(getCommonDataype(left, right, system) == null) {
			
			
			Pair<? extends SqlExpr, ? extends SqlExpr> pair = resolveCast(left, right, system);
			if(pair == null) {
				return SqlExprValue.FALSE;
			}
			
			if(getCommonDataype(pair.getKey(), pair.getValue(), system) == null) {
				return SqlExprValue.FALSE;
			} else {
				return new S_Equals(pair.getKey(), pair.getValue());
			}
			
		} else {
			result = new S_Equals(left, right);
		}
		
		return result;
	}
*/
}
