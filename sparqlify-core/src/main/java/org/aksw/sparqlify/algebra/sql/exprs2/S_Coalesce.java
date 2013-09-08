package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.apache.jena.atlas.io.IndentedWriter;


public class S_Coalesce
	extends SqlExprN
{	
	public S_Coalesce(TypeToken typeToken, List<SqlExpr> args) {
		super(typeToken, "coalesce", args);
	}

	public static SqlExpr create(SqlExpr... args) {
		return create(Arrays.asList(args));
	}
	
	
	/**
	 * The return type of S_Coalesce is taken from the first argument
	 * 
	 * @param args
	 * @return
	 */
	public static SqlExpr create(List<SqlExpr> args) {
		SqlExpr result;
		
		if(args.isEmpty()) {
			result = S_Constant.create(new SqlValue(TypeToken.TypeError, null));
			//return S_Constant.
		}
		else {
		
			SqlExpr firstArg = args.get(0);
		
			if(args.size() == 1) {
				return firstArg; 
			}
			else {
				TypeToken type = firstArg.getDatatype();
				result = new S_Coalesce(type, args);
			}
		}
		
		return result;
	}

	@Override
	public S_Coalesce copy(List<SqlExpr> args) {
		S_Coalesce result = new S_Coalesce(args.get(0).getDatatype(), args);
		return result;
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("Coalesce");
		writeArgs(writer);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
