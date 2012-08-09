package functions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class UrlEncode extends FunctionBase1 {
	private static final Logger logger = LoggerFactory.getLogger(UrlEncode.class);
	
	@Override
	public NodeValue exec(NodeValue v) {
		try {
			return NodeValue
					.makeString(URLEncoder.encode(v.getString(), "UTF8"));
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unexpected exception", e);
			return null;
		}
	}
}