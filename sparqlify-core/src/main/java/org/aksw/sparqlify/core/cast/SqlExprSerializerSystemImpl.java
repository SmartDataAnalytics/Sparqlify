package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprConstant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprVar;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;

public class SqlExprSerializerSystemImpl
	implements SqlExprSerializerSystem
{
	private Map<String, SqlFunctionSerializer> nameToSerializer = new HashMap<String, SqlFunctionSerializer>();

	private DatatypeToStringPostgres typeSerializer;
	private SqlLiteralMapper sqlLiteralMapper;

	public SqlExprSerializerSystemImpl(DatatypeToStringPostgres typeSerializer, SqlLiteralMapper sqlLiteralMapper) {
		this.typeSerializer = typeSerializer;
		this.sqlLiteralMapper = sqlLiteralMapper;
	}
	
	@Override
	public void addSerializer(String functionName, SqlFunctionSerializer serializer) {
		nameToSerializer.put(functionName, serializer);
	}

	@Override
	public String serialize(SqlExpr expr) {
		String result;
		if(expr.isConstant()) {
		
			SqlExprConstant c = expr.asConstant();
			SqlValue sqlValue = c.getValue();
			Object o = sqlValue.getValue();
			
			if(o == null) {
				Factory1<String> nullSerializer = typeSerializer.asString(c.getDatatype());
				result = nullSerializer.create("NULL");
			} else {			
				result = sqlLiteralMapper.serialize(sqlValue);
			}
		
		} else if(expr.isFunction()) {

			
			SqlExprFunction f = expr.asFunction();
			List<SqlExpr> args = f.getArgs();
			List<String> strs = new ArrayList<String>(args.size());
			for(SqlExpr arg : args) {
				String str = serialize(arg);
				
				strs.add(str);
			}
			
			String functionName = f.getName();
			
			if(functionName.equals("cast")) {
				Factory1<String> castSerializer = typeSerializer.asString(f.getDatatype());
				
				assert args.size() == 1 : "Excactly one argument expected for cast, got: " + args;
				
				String argStr = strs.get(0);
				
				result = castSerializer.create(argStr);
				
			} else {
			
				SqlFunctionSerializer serializer = nameToSerializer.get(functionName);
				if(serializer == null) {
					throw new RuntimeException("No serializer defined for: " + functionName + " in " + expr);
				}
				
				result = serializer.serialize(strs);
			}
			
		} else if(expr.isVariable()) {
			
			// TODO Make column name serialization configurable
			SqlExprVar v = expr.asVariable();
			S_ColumnRef ref = (S_ColumnRef)v;

			result = "\"" + ref.getColumnName() + "\"";
			
			if(ref.getRelationAlias() != null) {
				result = ref.getRelationAlias() + "." + result;
			} 
			
		} else {
		
			throw new RuntimeException("Should not happen");
			
		}

		return result;
	}

}
