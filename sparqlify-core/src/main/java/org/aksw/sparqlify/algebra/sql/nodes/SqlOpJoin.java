package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sdb.core.JoinType;


public class SqlOpJoin
	extends SqlOpBase2
{
	private JoinType joinType;
	//private boolean isLeftJoin;
	private List<SqlExpr> conditions;

	public SqlOpJoin(Schema schema, JoinType joinType, SqlOp left, SqlOp right) {
		this(schema, joinType, left, right, new ArrayList<SqlExpr>());
	}
	
	public SqlOpJoin(Schema schema, JoinType joinType, SqlOp left, SqlOp right, List<SqlExpr> conditions) {
		super(schema, left, right);
		this.joinType = joinType;
		this.conditions = conditions;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}



	public List<SqlExpr> getConditions() {
		return conditions;
	}
	
	
	public static Schema createJoinSchema(Schema a, Schema b) {
		List<String> names = new ArrayList<String>();
		names.addAll(a.getColumnNames());
		names.addAll(b.getColumnNames());
		
		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.putAll(a.getTypeMap());
		typeMap.putAll(b.getTypeMap());
		
		
		Schema result = new SchemaImpl(names, typeMap);

		return result;
	}

	public static SqlOpJoin create(JoinType joinType, SqlOp a, SqlOp b) {
		SqlOpJoin result = create(joinType, a, b, new ArrayList<SqlExpr>());
		
		return result;
	}

	
	public static SqlOpJoin create(JoinType joinType, SqlOp a, SqlOp b, List<SqlExpr> conditions) {
		
		Schema newSchema = createJoinSchema(a.getSchema(), b.getSchema());
		
		SqlOpJoin result = new SqlOpJoin(newSchema, joinType, a, b, conditions);
		
		return result;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpJoin " + joinType + "(");
		
		writer.incIndent();
		left.write(writer);
		writer.println(",");
		right.write(writer);
		writer.println();
		writer.decIndent();
		
		
		writer.print(")");
	}

	@Override
	public boolean isEmpty() {
		
		boolean result;
		
		switch(joinType) {
		case INNER: {
			boolean a = left.isEmpty();
			boolean b = right.isEmpty();
			result = a || b;
			break;
		}
		case LEFT: {
			result = left.isEmpty();
			break;
		}
		default: {
			throw new RuntimeException("Should not happen");
		}
		}
		
		return result;
	}

}
