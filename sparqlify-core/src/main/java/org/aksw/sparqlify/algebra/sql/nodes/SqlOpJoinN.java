package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlOpJoinN
	extends SqlOpBaseN
{	
	
	private static final Logger logger = LoggerFactory.getLogger(SqlOpJoinN.class);

	private String aliasName;
	
	
	public SqlOpJoinN(Schema schema, List<SqlOp> subOps) {
		this(schema, subOps, null);
	}

	public SqlOpJoinN(Schema schema, List<SqlOp> subOps, String aliasName) {
		super(schema, subOps);
		this.aliasName = aliasName;
	}
	
	public String getAliasName() {
		return aliasName;
	}


	public static SqlOpJoinN create(List<SqlOp> subOps) {
		SqlOpJoinN result = create(subOps, null);
		return result;
	}
	
	public static SqlOpJoinN create(List<SqlOp> subOps, String aliasName) {
		
		if(subOps.isEmpty()) {
			throw new RuntimeException("Cannot create union without any members");
		}
		
		
		
		/*
		 * Schema validation - all schemas must be equal in column names, order and types.
		 */
		List<String> firstColumnNames = null;
		Map<String, TypeToken> firstTypeMap = null;
		for(SqlOp member : subOps) {
			
			Schema schema = member.getSchema();
			
			List<String> columnNames = schema.getColumnNames();
			Map<String, TypeToken> typeMap = schema.getTypeMap();

			if(firstColumnNames == null) {
				firstColumnNames = columnNames;
				firstTypeMap = typeMap;
			} else {

				if(!(columnNames.equals(firstColumnNames) || typeMap.equals(firstTypeMap))) {
					logger.error("Union schema mismatch a:" + firstColumnNames + " | " + firstTypeMap);
					logger.error("Union schema mismatch b:" + columnNames + " | " + typeMap);
					throw new RuntimeException("Union schema mismatch");
				}
					
			}			
		}
		
		
		
		SqlOp pick = subOps.get(0);
		Schema schema = pick.getSchema();
		
		return new SqlOpJoinN(schema, subOps, aliasName);
	}

	@Override
	public boolean isEmpty() {
		for(SqlOp member : subOps) {
			if(!member.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
}
