package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.aksw.commons.collections.MapUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprConstant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprVar;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;

public class SqlExprSerializerSystemImpl
	implements SqlExprSerializerSystem
{
	private Map<String, SqlFunctionSerializer> nameToSerializer = new HashMap<String, SqlFunctionSerializer>();

	private DatatypeToStringPostgres typeSerializer;
	private SqlLiteralMapper sqlLiteralMapper;
	private SqlEscaper sqlEscaper;

	public SqlExprSerializerSystemImpl(DatatypeToStringPostgres typeSerializer, SqlEscaper sqlEscaper, SqlLiteralMapper sqlLiteralMapper) {
		this.typeSerializer = typeSerializer;
		this.sqlEscaper = sqlEscaper;
		this.sqlLiteralMapper = sqlLiteralMapper;
	}
	
	@Override
	public void addSerializer(String functionName, SqlFunctionSerializer serializer) {
		nameToSerializer.put(functionName, serializer);
	}

	@Override
	public void addSerializer(Collection<String> functionIds, SqlFunctionSerializer serializer) {
		MapUtils.putForAll(nameToSerializer, functionIds, serializer);
	}

	
	@Override
	public String serialize(SqlExpr expr) {
		String result;
		if(expr.isConstant()) {
		
			SqlExprConstant c = expr.asConstant();
			SqlValue sqlValue = c.getValue();
			Object o = sqlValue.getValue();
			
			if(o == null) {
				UnaryOperator<String> nullSerializer = typeSerializer.asString(c.getDatatype());
				result = nullSerializer.apply("NULL");
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
				UnaryOperator<String> castSerializer = typeSerializer.asString(f.getDatatype());
				
				assert args.size() == 1 : "Excactly one argument expected for cast, got: " + args;
				
				String argStr = strs.get(0);
				
				result = castSerializer.apply(argStr);
				
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

			result = sqlEscaper.escapeColumnName(ref.getColumnName());
			//result = "\"" + ref.getColumnName() + "\"";
			
			if(ref.getRelationAlias() != null) {
			    String alias = sqlEscaper.escapeAliasName(ref.getRelationAlias());
				result = alias + "." + result;
			} 
			
		} else {
		
			throw new RuntimeException("Should not happen");
			
		}

		return result;
	}

}
