package org.aksw.sparqlify.algebra.sparql.expr;

import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A 'permissive' version of Jena's E_StrConcat, that does not
 * complain when mixing types (e.g. concat(string, int)).
 * 
 * 
 * @author raven
 *
 */
public class E_StrConcatPermissive extends ExprFunctionN
{
    private static final String name = "concat" ;
    
    public E_StrConcatPermissive(ExprList args)
    {
        super(name, args) ;
    }

    @Override
    public Expr copy(ExprList newArgs)
    {
        return new E_StrConcatPermissive(newArgs) ;
    }

    @Override
    public NodeValue eval(List<NodeValue> args)
    { 
    	String str = "";
    	for(NodeValue arg : args) {
    		str += arg.asUnquotedString();
    	}
        return NodeValue.makeString(str);
    }
}