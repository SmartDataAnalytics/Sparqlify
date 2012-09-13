package functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase4;

public class RdfTerm
	extends FunctionBase4
{
	private static final Logger logger = LoggerFactory.getLogger(RdfTerm.class);
	
	public static String toLexicalForm(NodeValue nodeValue) {
		return nodeValue.asUnquotedString();
	}

	/**
	 * type, lexical value, language tag, data type
	 * 
	 */
	@Override
	public NodeValue exec(NodeValue type, NodeValue node, NodeValue langTag, NodeValue datatype)
	{
		return eval(type, node, langTag, datatype);
	}
	
	public static NodeValue eval(NodeValue type, NodeValue node, NodeValue langTag, NodeValue datatype)
	{	
		String lexicalForm = toLexicalForm(node);
		int typeValue = type.getDecimal().intValue();
		switch(typeValue) {
		case 0: // Blank Node
			return NodeValue.makeNode(Node.createAnon(new AnonId(toLexicalForm(node))));			
		case 1: // URI
			return NodeValue.makeNode(Node.createURI(toLexicalForm(node)));
		case 2: // Plain Literal
			String dt = toLexicalForm(datatype);
			if(dt != null && !dt.isEmpty()) {
				logger.warn("Language tag should be null or empty, was '" + dt + "'");
			}
			return NodeValue.makeNode(lexicalForm, toLexicalForm(langTag), (String)null);
		case 3: // Typed Literal
			String lang = toLexicalForm(langTag);
			if(lang != null && !lang.isEmpty()) {
				logger.warn("Language tag should be null or empty, was '" + lang + "'");
			}
			return NodeValue.makeNode(lexicalForm, null, toLexicalForm(datatype));
		}
		
		throw new RuntimeException("Invalid type-value for RdfTerm: " + typeValue);
		//return null;
	}

	
	/* Old version - assumes strings for lang and datatype
	@Override
	public NodeValue exec(NodeValue type, NodeValue node, NodeValue langTag, NodeValue datatype)
	{
		String lexicalForm = toLexicalForm(node);
		int typeValue = type.getDecimal().intValue();
		switch(typeValue) {
		case 0: // Blank Node
			return NodeValue.makeNode(Node.createAnon(new AnonId(node.getString())));			
		case 1: // URI
			return NodeValue.makeNode(Node.createURI(node.getString()));
		case 2: // Plain Literal
			String dt = datatype.getString();
			if(dt != null && !dt.isEmpty()) {
				logger.warn("Language tag should be null or empty, was '" + dt + "'");
			}
			return NodeValue.makeNode(lexicalForm, langTag.getString(), (String)null);
		case 3: // Typed Literal
			String lang = langTag.getString();
			if(lang != null && !lang.isEmpty()) {
				logger.warn("Language tag should be null or empty, was '" + lang + "'");
			}
			return NodeValue.makeNode(lexicalForm, null, datatype.getString());
		}
		
		throw new RuntimeException("Invalid type-value for RdfTerm: " + typeValue);
		//return null;
	}
*/
}
