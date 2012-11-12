package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.Pair;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Serialize;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator2;
import org.aksw.sparqlify.core.datatypes.SqlFunctionSerializer;



/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_Compare
	extends SqlExprEvaluator2
{
	private DatatypeSystem datatypeSystem;
	private SqlFunctionSerializer serializer;
	
	private String symbol;
	
	public SqlExprEvaluator_Compare(String symbol, DatatypeSystem datatypeSystem) {
		this.datatypeSystem = datatypeSystem;
		this.serializer = new SqlFunctionSerializerOp2(symbol);
	}
	
	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		//SqlExpr result = SqlExprOps.logicalAnd(a, b);
		//return result;
		if(a.equals(S_Constant.TYPE_ERROR) || b.equals(S_Constant.TYPE_ERROR)) {
			return S_Constant.TYPE_ERROR;
		}

		if(getCommonDataype(a, b, datatypeSystem) == null) {
			
			
			Pair<? extends SqlExpr, ? extends SqlExpr> pair = resolveCast(a, b, datatypeSystem);
			if(pair == null) {
				return S_Constant.TYPE_ERROR;
			}
			
			if(getCommonDataype(pair.getKey(), pair.getValue(), datatypeSystem) == null) {
				return S_Constant.TYPE_ERROR;
			} else {
				SqlExpr result = new S_Serialize(TypeToken.Boolean, symbol, Arrays.asList(pair.getKey(), pair.getValue()), serializer);
				//return new S_Equals(pair.getKey(), pair.getValue());
				return result;
			}
			
		}
		
		
		SqlExpr result = new S_Serialize(TypeToken.Boolean, symbol, Arrays.asList(a, b), serializer);
		return result;

		
		
//		SqlExpr result;
//		
//		System.err.println("TODO: Check datatypes properly");
//		if(a.getDatatype().equals(b.getDatatype())) {
//			//Method
//			//XMethodImpl x = new XMethodImpl("=", null, null, serializer);
//
//			//SqlExpr result = new S_Method(x, Arrays.asList(a, b));
//			//return result;
//			result = new S_Serialize(TypeToken.Boolean, "=", Arrays.asList(a, b), serializer);
//		} else {
//			result = S_Constant.TYPE_ERROR;
//		}
//		
//		return result;
		
		// TODO Rule out incompatible datatype combinations
		//return S_Equals.create(a, b);
	}
	
	
	
	public static TypeToken getCommonDataype(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		Set<TypeToken> commons = system.supremumDatatypes(left.getDatatype(), right.getDatatype());

		// TODO We should probably return type error here
		if(commons.isEmpty()) {
			return null;
		}

		if(commons.size() > 1) {
			throw new RuntimeException("Ambiguous type candidates: " + commons);
		}
		
		return commons.iterator().next();
	}
	
	public static S_Constant asConstant(SqlExpr expr) {
		return (expr instanceof S_Constant)
				? (S_Constant)expr
				: null;
	}
	
	public static S_ColumnRef asColumn(SqlExpr expr) {
		return (expr instanceof S_ColumnRef)
				? (S_ColumnRef)expr
				: null;		
	}
	
	
//	public static S_Constant tryCast(S_Constant value, SqlDatatype datatype) {
//		return value;
//	}
	
	public static Pair<? extends SqlExpr, ? extends SqlExpr> resolveCast(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		Pair<S_ColumnRef, S_Constant> pair = tryMatch(left, right);
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
		
		
		Object value = pair.getValue().getValue();

		TypeToken targetType = pair.getKey().getDatatype();
		Object castedValue = system.cast(value, targetType);
		if(castedValue == null) {
			return null;
		}
		
		
		return Pair.create(pair.getKey(), new S_Constant(targetType, castedValue));
		
	}
	
	public static Pair<S_ColumnRef, S_Constant> tryMatch(SqlExpr left, SqlExpr right) {
		Pair<S_ColumnRef, S_Constant> result = tryMatchDirected(left, right);
		if(result == null) {
			result = tryMatchDirected(right, left);
		}
		
		return result;
	}
	
	public static Pair<S_ColumnRef, S_Constant> tryMatchDirected(SqlExpr left, SqlExpr right) {
		S_ColumnRef column = asColumn(left);
		if(column == null) {
			return null;
		}
		
		S_Constant value = asConstant(right);
		if(value == null) {
			return null;
		}
		
		return Pair.create(column, value);
	}
	
}

