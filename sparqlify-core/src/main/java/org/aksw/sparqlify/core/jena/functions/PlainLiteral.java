package org.aksw.sparqlify.core.jena.functions;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase;
import com.hp.hpl.jena.sparql.util.Utils;

public class PlainLiteral
	extends FunctionBase
{

	public NodeValue exec(NodeValue value, NodeValue lang) {
		String v = value.asUnquotedString();
		
		Node tmp;
		if(lang == null) {
			tmp = Node.createLiteral(v);			
		} else {
			String l = lang == null ? null : lang.asUnquotedString();

			tmp = Node.createLiteral(v, l, false);
		}
			
		

		
		NodeValue result = NodeValue.makeNode(tmp);
		return result;
	}


	@Override
	public NodeValue exec(List<NodeValue> args) {
		
		NodeValue argLang = args.size() > 1 ? args.get(1) : null;
		
		NodeValue result = exec(args.get(0), argLang);		
		return result;
	}


	@Override
	public void checkBuild(String uri, ExprList args) {
		if(args.size() == 0 || args.size() > 2) {
            throw new QueryBuildException("Function '"+Utils.className(this)+"' takes one or two arguments") ;
		}
	}
}
