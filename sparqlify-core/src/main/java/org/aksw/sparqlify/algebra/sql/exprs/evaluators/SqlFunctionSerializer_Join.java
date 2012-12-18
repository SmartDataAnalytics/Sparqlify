package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * Serializer that joins the arguments on a given symbol 
 * 
 * Used e.g. for converting concat(a, ..., z) into Postgres' a || ... || z
 * 
 * @author raven
 *
 */
public class SqlFunctionSerializer_Join
	implements SqlFunctionSerializer
{
	private String separator;
	
	public SqlFunctionSerializer_Join(String separator) {
		this.separator = separator;
	}

	@Override
	public String serialize(List<String> args) {
		String result = Joiner.on(separator).join(args);
		return result;
	}
}
