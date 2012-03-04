package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;


public class S_Regex
	extends SqlExpr1
{
	private String pattern;
	private String flags;

	public S_Regex(SqlExpr expr, String pattern, String flags) {
		super(expr, DatatypeSystemDefault._BOOLEAN);
		
		this.pattern = pattern;
		this.flags = flags;
	}

	public String getPattern() {
		return pattern;
	}

	public String getFlags() {
		return flags;
	}

	
}
