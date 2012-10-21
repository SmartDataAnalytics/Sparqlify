package org.aksw.sparqlify.core.datatypes;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Serialize;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;

class SqlFunctionSerializerOp2
	extends SqlFunctionSerializerBase2
{
	private String opSymbol;
	
	public SqlFunctionSerializerOp2(String opSymbol) {
		this.opSymbol = opSymbol;
	}


	@Override
	public String serialize(String a, String b) {
		String result = a + " " + opSymbol + " " + b;
		return result;
	}
}

abstract class SqlFunctionSerializerBase2
	implements SqlFunctionSerializer
{		
	@Override
	public String serialize(List<String> args) {
		if(args.size() != 2) {
			throw new RuntimeException("Exactly 2 arguments expected, got: " + args);
		}
		
		String result = serialize(args.get(0), args.get(1));
		return result;
	}
	
	public abstract String serialize(String a, String b);	
}




/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_Equals
	extends SqlExprEvaluator2
{
	private DatatypeSystem datatypeSystem;
	private SqlFunctionSerializer serializer;
	
	public SqlExprEvaluator_Equals(DatatypeSystem datatypeSystem) {
		this.datatypeSystem = datatypeSystem;
		this.serializer = new SqlFunctionSerializerOp2("=");
	}
	
	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		//SqlExpr result = SqlExprOps.logicalAnd(a, b);
		//return result;
		if(a.equals(S_Constant.TYPE_ERROR) || b.equals(S_Constant.TYPE_ERROR)) {
			return S_Constant.TYPE_ERROR;
		}
		
		SqlExpr result;
		
		System.err.println("TODO: Check datatypes properly");
		if(a.getDatatype().equals(b.getDatatype())) {
			//Method
			//XMethodImpl x = new XMethodImpl("=", null, null, serializer);

			//SqlExpr result = new S_Method(x, Arrays.asList(a, b));
			//return result;
			result = new S_Serialize(TypeToken.Boolean, "=", Arrays.asList(a, b), serializer);
		} else {
			result = S_Constant.TYPE_ERROR;
		}
		
		return result;
		
		// TODO Rule out incompatible datatype combinations
		//return S_Equals.create(a, b);
	}
}
