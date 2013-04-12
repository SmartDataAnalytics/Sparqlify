package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.Set;

import org.aksw.commons.util.Pair;
import org.aksw.commons.util.factory.Factory2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.cast.TypeSystem;



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
	private TypeSystem datatypeSystem;
	private Factory2<SqlExpr> exprFactory;

	//private SqlFunctionSerializer serializer;
		
	public SqlExprEvaluator_Compare(TypeSystem datatypeSystem, Factory2<SqlExpr> exprFactory) {
		this.datatypeSystem = datatypeSystem;
		this.exprFactory = exprFactory;
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
				SqlExpr result = exprFactory.create(pair.getKey(), pair.getValue());
				
				//SqlExpr result = new S_Serialize(TypeToken.Boolean, symbol, Arrays.asList(pair.getKey(), pair.getValue()), serializer);
				//return new S_Equals(pair.getKey(), pair.getValue());
				return result;
			}
			
		}
		
		
		SqlExpr result = exprFactory.create(a, b);
		//SqlExpr result = new S_Serialize(TypeToken.Boolean, symbol, Arrays.asList(a, b), serializer);
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
	
	
	
	public static TypeToken getCommonDataype(SqlExpr left, SqlExpr right, TypeSystem system) {
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
	
	public static Pair<? extends SqlExpr, ? extends SqlExpr> resolveCast(SqlExpr left, SqlExpr right, TypeSystem system) {
		Pair<S_ColumnRef, S_Constant> pair = tryMatch(left, right);
		if(pair == null) {
			return Pair.create(left, right);
		}

		try {
		
		if(pair.getKey().getDatatype().equals(pair.getValue().getDatatype())) {
			return pair;
		}
		
		} catch(Throwable t) {
			// Not sure if this should happen
			throw new RuntimeException("Should not happen", t); 
			//System.out.println("ffs");
		}
		
		
		SqlValue value = pair.getValue().getValue();

		TypeToken targetType = pair.getKey().getDatatype();
		SqlValue castedValue = system.cast(value, targetType);
		if(castedValue == null) {
			return null;
		}
		
		S_Constant newValue = new S_Constant(castedValue);
		Pair<S_ColumnRef, S_Constant> result = Pair.create(pair.getKey(), newValue);
		return result;
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

