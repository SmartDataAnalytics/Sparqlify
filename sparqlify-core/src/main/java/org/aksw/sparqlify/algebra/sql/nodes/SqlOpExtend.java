package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.core.SqlDatatype;
import org.aksw.sparqlify.core.algorithms.DatatypeAssigner;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.expr.Expr;

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
	
	
	public static Map<String, SqlDatatype> extractExtendedTypeMap(Map<String, Expr> nameToExpr, DatatypeAssigner datatypeAssigner, Map<String, SqlDatatype> baseTypeMap) {
		Map<String, SqlDatatype> result = new HashMap<String, SqlDatatype>();
		for(Entry<String, Expr> entry : nameToExpr.entrySet()) {
			
			Expr expr = entry.getValue();
			SqlDatatype datatype = datatypeAssigner.assign(expr, baseTypeMap);
			
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
		
		Map<String, SqlDatatype> typeMap = extractExtendedTypeMap(projection.getNameToExpr(), datatypeAssigner, op.getSchema().getTypeMap());
		
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
