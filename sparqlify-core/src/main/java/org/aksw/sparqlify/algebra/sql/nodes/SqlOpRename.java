package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;


/**
 * The rename operator from the relational algebra.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlOpRename
	extends SqlOpBase1
{

	/**
	 * Old name -&gt; New name
	 */
	private BidiMap<String, String> rename;
	
	public SqlOpRename(Schema schema, SqlOp subOp, BidiMap<String, String> rename) {
		super(schema, subOp);
		
		this.rename = rename;
	}
	
	public BidiMap<String, String> getRename() {
		return rename;
	}

	
	public static SqlOpRename create(SqlOp op, Map<String, String> map) {
		
		BidiMap<String, String> rename = new DualHashBidiMap<String, String>(map);

		Schema oldSchema = op.getSchema();
		
		List<String> newNames = new ArrayList<String>();
		Map<String, SqlDatatype> newTypeMap = new HashMap<String, SqlDatatype>();
		
		for(String oldName : oldSchema.getColumnNames()) {
			SqlDatatype datatype = oldSchema.getColumnType(oldName);
			String newName = rename.get(oldName);
			
			if(newName == null) {
				newName = oldName;
			}
			
			newNames.add(newName);
			newTypeMap.put(newName, datatype);
		}
		
		Schema newSchema = SchemaImpl.create(newNames, newTypeMap);
		
		SqlOpRename result = new SqlOpRename(newSchema, op, rename);
		
		return result;
		
		//old.get
		/*
		if(true) {
			throw new RuntimeException("Not implemented yet");
		}
		
		
		
		boolean doMerge = true;
		if(doMerge) {
			
		}
		*/		
		
		//return new SqlOpProject();
	}
}
