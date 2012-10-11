package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.datatypes.XClass;
import org.openjena.atlas.io.IndentedWriter;

/**
 * This is actually a combination of extend, rename and project.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlOpProject
	extends SqlOpBase1
{
	private List<String> columnNames;
	
	public SqlOpProject(Schema schema, SqlOp subOp, List<String> columnNames) {
		super(schema, subOp);
		this.columnNames = columnNames;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}

	
	public static SqlOpProject create(SqlOp subOp, List<String> columnNames) {		
		
		Map<String, XClass> typeMap = subOp.getSchema().getTypeMap();
		
		Schema newSchema = new SchemaImpl();

		assert subOp.getSchema().getColumnNames().containsAll(columnNames)
			:  "Projection must not reference undefined columns; "
			+ "referenced: " + columnNames
			+ ", defined: " + subOp.getSchema().getColumnNames();
		
		for(String name : columnNames) {
			
			newSchema.getColumnNames().add(name);
			
			XClass type = typeMap.get(name);
			newSchema.getTypeMap().put(name, type);
		}
		
		
		SqlOpProject result = new SqlOpProject(newSchema, subOp, columnNames);
		
		return result;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpProject" + columnNames + "(");
		
		writer.incIndent();
		subOp.write(writer);
		writer.println();
		writer.decIndent();
		
		writer.print(")");
	}

}