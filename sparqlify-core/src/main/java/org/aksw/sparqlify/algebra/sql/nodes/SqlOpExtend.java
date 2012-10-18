package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class SqlOpExtend
	extends SqlOpBase1
{
	private Projection projection;

	public SqlOpExtend(Schema schema, SqlOp subOp, Projection projection) {
		super(schema, subOp);
		this.projection = projection;
	}

	public Projection getProjection() {
		return projection;
	}
	
	
	/*
	public static Map<String, XClass> extractExtendedTypeMap(Map<String, Expr> nameToExpr, DatatypeAssigner datatypeAssigner, Map<String, XClass> baseTypeMap) {
		Map<String, XClass> result = new HashMap<String, XClass>();
		for(Entry<String, Expr> entry : nameToExpr.entrySet()) {
			
			Expr expr = entry.getValue();
			XClass datatype = datatypeAssigner.assign(expr, baseTypeMap);
			
			if(datatype == null) {
				System.err.println("Could not determine datatype for: " + expr);
			}
			
			result.put(entry.getKey(), datatype);
		}
		
		return result;
	}
	
	
	public static SqlOpExtend create(SqlOp op, Projection projection, DatatypeAssigner datatypeAssigner) {
		
		Schema schema = new SchemaImpl();
		schema.getColumnNames().addAll(op.getSchema().getColumnNames());
		schema.getColumnNames().addAll(projection.getNames());
		
		Map<String, XClass> typeMap = extractExtendedTypeMap(projection.getNameToExpr(), datatypeAssigner, op.getSchema().getTypeMap());
		
		//projection.getNameToExpr(typeMap)
		
		schema.getTypeMap().putAll(op.getSchema().getTypeMap());
		schema.getTypeMap().putAll(typeMap);
		
		
		return new SqlOpExtend(schema, op, projection);
	}
*/

	public static Map<String, TypeToken> extractExtendedTypeMap(Map<String, SqlExpr> nameToExpr, Map<String, TypeToken> baseTypeMap) {
		Map<String, TypeToken> result = new HashMap<String, TypeToken>();
		for(Entry<String, SqlExpr> entry : nameToExpr.entrySet()) {
			
			SqlExpr expr = entry.getValue();
			//XClass datatype = datatypeAssigner.assign(expr, baseTypeMap);
			TypeToken datatype = expr.getDatatype();
			
			if(datatype == null) {
				System.err.println("Could not determine datatype for: " + expr);
			}
			
			result.put(entry.getKey(), datatype);
		}
		
		return result;
	}

	public static Map<String, TypeToken> extractExtendedTypeMap(Map<String, SqlExpr> nameToExpr) {
		Map<String, TypeToken> result = new HashMap<String, TypeToken>();
		for(Entry<String, SqlExpr> entry : nameToExpr.entrySet()) {
			
			/*
			Expr expr = entry.getValue();
			XClass datatype = datatypeAssigner.assign(expr, baseTypeMap);
			
			if(datatype == null) {
				System.err.println("Could not determine datatype for: " + expr);
			}
			*/
			
			TypeToken typeName = entry.getValue().getDatatype();
			result.put(entry.getKey(), typeName);
		}
		
		return result;
	}

	public static SqlOpExtend create(SqlOp op, Projection projection) {
		
		Schema schema = new SchemaImpl();
		schema.getColumnNames().addAll(op.getSchema().getColumnNames());
		schema.getColumnNames().addAll(projection.getNames());
		
		Map<String, TypeToken> typeMap = extractExtendedTypeMap(projection.getNameToExpr(), op.getSchema().getTypeMap());
		
		//projection.getNameToExpr(typeMap)
		
		schema.getTypeMap().putAll(op.getSchema().getTypeMap());
		schema.getTypeMap().putAll(typeMap);
		
		
		return new SqlOpExtend(schema, op, projection);
	}

	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpExtend " + projection + "(");
		
		writer.incIndent();
		subOp.write(writer);
		writer.println();
		writer.decIndent();
		
		writer.print(")");
	}
}
