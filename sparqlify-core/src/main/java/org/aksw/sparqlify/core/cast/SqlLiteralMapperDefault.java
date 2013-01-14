package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.expr.util.NodeValueUtils;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class SqlLiteralMapperDefault
	implements SqlLiteralMapper
{
	@Override
	public String serialize(NodeValue value) {
		Object o = NodeValueUtils.getValue(value);

		String result;
		if(o instanceof Number) {
			result = "" + o;
		} else {
			result = "\"" + o + "\"";
		}

		return result;
	}
}
