package org.aksw.sparqlify.util;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;

public class SqlOpUtils {
	
	/**
	 * Returns the table name if the sqlOp is of type SqlTable.
	 * null otherwise.
	 * 
	 * @param sqlOp
	 * @return
	 */
	public static String getTableName(SqlOp sqlOp) {
		String result = null;
		if(sqlOp instanceof SqlOpTable) {
			SqlOpTable sqlTable = (SqlOpTable)sqlOp;
			
			result = sqlTable.getTableName();
		}
		
		return result;
	}
}
