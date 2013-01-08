package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlOpUnionN
	extends SqlOpBaseN
{	
	
	private static final Logger logger = LoggerFactory.getLogger(SqlOpUnionN.class);

	private String aliasName;
	
	
	public SqlOpUnionN(Schema schema, List<SqlOp> subOps) {
		this(schema, subOps, null);
	}

	public SqlOpUnionN(Schema schema, List<SqlOp> subOps, String aliasName) {
		super(schema, subOps);
		this.aliasName = aliasName;
	}
	
	public String getAliasName() {
		return aliasName;
	}


	public static SqlOpUnionN create(List<SqlOp> subOps) {
		SqlOpUnionN result = create(subOps, null);
		return result;
	}

	public static SqlOpUnionN create(List<SqlOp> subOps, String aliasName) {
		
		if(subOps.isEmpty()) {
			throw new RuntimeException("Cannot create union without any members");
		}
		
		logger.warn("FIXME schemas of union members not validated");
		
		SqlOp pick = subOps.get(0);
		Schema schema = pick.getSchema();
		
		return new SqlOpUnionN(schema, subOps, aliasName);
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
