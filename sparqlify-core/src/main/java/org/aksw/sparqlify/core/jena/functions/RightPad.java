package org.aksw.sparqlify.core.jena.functions;

import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

/**
 * Right pad the string value of the first argument
 * 
 * @author raven
 *
 */
public class RightPad
	extends FunctionBase2
{
	@Override
	public NodeValue exec(NodeValue value, NodeValue size) {
		String v = value.asUnquotedString();
		int n = NodeValueUtils.getInteger(size);

		String padded = StringUtils.rightPad(v, n);
		NodeValue result = NodeValue.makeString(padded);

		return result;
	}
}
