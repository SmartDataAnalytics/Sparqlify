package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.SqlDatatype;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sparql.expr.ExprList;


public class SqlOpJoin
	extends SqlOpBase2
{
	private JoinType joinType;
	//private boolean isLeftJoin;
	private ExprList conditions;
	
	public SqlOpJoin(Schema schema, JoinType joinType, SqlOp left, SqlOp right, ExprList conditions) {
		super(schema, left, right);
		this.joinType = joinType;
		this.conditions = conditions;
	}
	
	public JoinType getJoinType() {
		return joinType;
	}



	public ExprList getConditions() {
		return conditions;
	}
	
	
	public static Schema createJoinSchema(Schema a, Schema b) {
		List<String> names = new ArrayList<String>();
		names.addAll(a.getColumnNames());
		names.addAll(b.getColumnNames());
		
		Map<String, SqlDatatype> typeMap = new HashMap<String, SqlDatatype>();
		typeMap.putAll(a.getTypeMap());
		typeMap.putAll(b.getTypeMap());
		
		
		Schema result= new SchemaImpl(names, typeMap);

		return result;
	}

	public static SqlOpJoin create(JoinType joinType, SqlOp a, SqlOp b) {
		SqlOpJoin result = create(joinType, a, b, new ExprList());
		
		return result;
	}

	
	public static SqlOpJoin create(JoinType joinType, SqlOp a, SqlOp b, ExprList conditions) {
		
		Schema newSchema = createJoinSchema(a.getSchema(), b.getSchema());
		
		SqlOpJoin result = new SqlOpJoin(newSchema, JoinType.INNER, a, b, conditions);
		
		return result;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpJoin(");
		
		writer.incIndent();
		left.write(writer);
		writer.println(",");
		right.write(writer);
		writer.println();
		writer.decIndent();
		
		
		writer.print(")");
	}

}
