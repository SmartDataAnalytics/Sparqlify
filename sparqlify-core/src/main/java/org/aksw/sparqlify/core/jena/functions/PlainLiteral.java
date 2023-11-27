package org.aksw.sparqlify.core.jena.functions;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class PlainLiteral
    extends FunctionBase
{

    public NodeValue exec(NodeValue value, NodeValue lang) {
        String v = value.asUnquotedString();

        Node tmp;
        if(lang == null) {
            tmp = NodeFactory.createLiteral(v);
        } else {
            String l = lang == null ? null : lang.asUnquotedString();

            tmp = NodeFactory.createLiteral(v, l);
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
            throw new QueryBuildException("Function '" + this.getClass().getName() + "' takes one or two arguments") ;
        }
    }
}
