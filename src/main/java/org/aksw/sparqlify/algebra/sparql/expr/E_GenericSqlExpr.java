package org.aksw.sparqlify.algebra.sparql.expr;

import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.SqlFunctionDefinition;
import org.apache.commons.lang.NotImplementedException;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class E_GenericSqlExpr
	extends ExprFunctionN 
{
	private SqlFunctionDefinition funcDef;
	
	// TODO Instances of this class are instances of function invocations. Add a pointer to the function definition.
	
    //private String sqlFunctionName;
    
    @SuppressWarnings("unchecked")
	public E_GenericSqlExpr(SqlFunctionDefinition funcDef, Expr ... args) {
    	this(funcDef, new ExprList(Arrays.asList(args))) ;
    }

    public E_GenericSqlExpr(SqlFunctionDefinition funcDef, ExprList args) {
    	super(funcDef.getName(), args);
    	this.funcDef = funcDef;
    }
	
    public SqlFunctionDefinition getFuncDef()
    {
    	return funcDef;
    }
    
	@Override
	protected NodeValue eval(List<NodeValue> args) {
		// TODO Invoke on some global registry
		throw new NotImplementedException();
	}

	@Override
	protected Expr copy(ExprList newArgs) {
		return new E_GenericSqlExpr(funcDef, newArgs);
	}
}
