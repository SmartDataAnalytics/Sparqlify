package org.aksw.sparqlify.core.jena.functions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

/**
 * The opposite of the Sparql standards' "encode_for_uri" (right now its my UrlEncode):
 * 
 * 
 * It takes a plain (string) literal, and url decodes it.
 * Any input of different type results in an exception.
 * 
 * Note:
 * The optimizer will transform
 *     UrlEncode(?x) = const into
 *     ?x = UrlDecode(const)
 *     
 * Since a single string may have multiple 
 * url encodings (every chacacter could be percent escaped), the implication is,
 * that the expression will evaluate true for all constants whose url-encoded
 * form equals x; as opposed to only the specific given constant.
 * 
 * I think, in practice this might be very convenient property, but if people dislike it,
 * I could add an uri-equals predicate, and only for this the optimization may be applied.
 * 
 * 
 * @author raven
 *
 */
public class UrlDecode
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue v) {
		try {
			return NodeValue.makeString(URLDecoder.decode(v.getString(), "UTF8"));
		} catch (UnsupportedEncodingException e) {
			// Will never catch something, will never give something back in return
			return null;
		}
	}	
}
