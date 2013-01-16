package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprConstant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprVar;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class SqlExprSerializerSystemImpl
	implements SqlExprSerializerSystem
{
	private Map<String, SqlFunctionSerializer> nameToSerializer = new HashMap<String, SqlFunctionSerializer>();

	private SqlLiteralMapper sqlLiteralMapper;
	
	public SqlExprSerializerSystemImpl(SqlLiteralMapper sqlLiteralMapper) {
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
			NodeValue nodeValue = c.getValue();
			result = sqlLiteralMapper.serialize(nodeValue);
		
		} else if(expr.isFunction()) {

			SqlExprFunction f = expr.asFunction();
			List<SqlExpr> args = f.getArgs();
			List<String> strs = new ArrayList<String>(args.size());
			for(SqlExpr arg : args) {
				String str = serialize(arg);
				
				strs.add(str);
			}

			
			String functionName = f.getName();			
			SqlFunctionSerializer serializer = nameToSerializer.get(functionName);
			if(serializer == null) {
				throw new RuntimeException("No serializer defined for: " + functionName + " in " + expr);
			}
			
			result = serializer.serialize(strs);
			
		} else if(expr.isVariable()) {
			
			// TODO Make column name serialization configurable
			SqlExprVar v = expr.asVariable();
			String varName = v.getVarName();
			
			result = "\"" + varName + "\"";
			
		} else {
		
			throw new RuntimeException("Should not happen");
			
		}

		return result;
	}

}
