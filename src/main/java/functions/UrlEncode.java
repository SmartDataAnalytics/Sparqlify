package functions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class UrlEncode extends FunctionBase1 {
	@Override
	public NodeValue exec(NodeValue v) {
		try {
			return NodeValue
					.makeString(URLEncoder.encode(v.getString(), "UTF8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}