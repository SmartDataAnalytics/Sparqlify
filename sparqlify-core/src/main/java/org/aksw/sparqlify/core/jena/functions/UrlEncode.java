package org.aksw.sparqlify.core.jena.functions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlEncode extends FunctionBase1 {
	private static final Logger logger = LoggerFactory.getLogger(UrlEncode.class);
	
	@Override
	public NodeValue exec(NodeValue v) {
		try {
			String str = v.getString();
			String encoded = URLEncoder.encode(str, "UTF8");
			String tmp = encoded.replace("+", "%20"); 

			NodeValue result = NodeValue
					.makeString(tmp);
			return result;
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unexpected exception", e);
			return null;
		}
	}
}