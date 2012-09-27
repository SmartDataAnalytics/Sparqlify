package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

public class SqlNodeUtil {
	public static Map<String, SqlDatatype> getColumnToDatatype(SqlNodeOld sqlNode) {
		Map<String, SqlDatatype> result = new HashMap<String, SqlDatatype>();
		
		for(Entry<String, SqlExpr> entry : sqlNode.getAliasToColumn().entrySet()) {
			result.put(entry.getKey(), entry.getValue().getDatatype());
		}
		
		return result;
	}
	
}
