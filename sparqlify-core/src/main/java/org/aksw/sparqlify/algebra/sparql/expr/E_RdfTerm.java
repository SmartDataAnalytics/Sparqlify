package org.aksw.sparqlify.algebra.sparql.expr;

import java.util.List;

import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.jena.functions.RdfTerm;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class E_RdfTerm
	extends ExprFunctionN
{
	public static E_RdfTerm createBlankNode(Expr expr) {
		return new E_RdfTerm(NodeValue.makeInteger(0), expr, NodeValue.makeString(""), NodeValue.makeString(""));
	}
	
	public static E_RdfTerm createUri(Expr expr) {
		return new E_RdfTerm(NodeValue.makeInteger(1), expr, NodeValue.makeString(""), NodeValue.makeString(""));
	}

	public static E_RdfTerm createPlainLiteral(Expr expr) {
		return new E_RdfTerm(NodeValue.makeInteger(2), expr, NodeValue.makeString(""), NodeValue.makeString(""));
	}
	
	public static E_RdfTerm createTypedLiteral(Expr expr, Expr datatype) {
		
		//DatatypeSystemDefault.
		return new E_RdfTerm(NodeValue.makeInteger(3), expr, NodeValue.makeString(""), datatype);
	}

	
	public E_RdfTerm(List<Expr> exprs) {
		this(exprs.get(0), exprs.get(1), exprs.get(2), exprs.get(3));

		
		/*
		if(exprs.size() != 4) {
			throw new IllegalArgumentException("ExprRdfTerm requires exactly four arguments");
		}*/	
	}
	
	public E_RdfTerm(Expr type, Expr lexicalValue, Expr languageTag, Expr datatype) {
		super(SparqlifyConstants.rdfTermLabel, type, lexicalValue, languageTag, datatype);
	}
	
	public Expr getType()
	{
		return super.getArgs().get(0);
	}

	public Expr getLexicalValue()
	{
		return super.getArgs().get(1);
	}
	
	public Expr getLanguageTag()
	{
		return super.getArgs().get(2);
	}
	
	public Expr getDatatype()
	{
		return super.getArgs().get(3);
	}

    @Override
    public boolean isConstant() 
    {
    	return false;
    	
    	/*
    	for(Expr expr : super.getArgs()) {
    		if(!expr.isConstant()) {
    			return false;
    		}
    	}
    	
    	return true;
    	*/
    }
    
    @Override
    public NodeValue getConstant() {
    	NodeValue result = RdfTerm.eval(
    			this.getArgs().get(0).getConstant(),
    			this.getArgs().get(1).getConstant(),
    			this.getArgs().get(2).getConstant(),
    			this.getArgs().get(3).getConstant()
    		);
    	
    	//System.err.println(result);
    	
    	return result;
    }

	
	@Override
	protected NodeValue eval(List<NodeValue> args) {
		return RdfTerm.eval(args.get(0), args.get(1), args.get(2), args.get(3));
		//RdfTerm
		//throw new RuntimeException("Should not happen");
		// TODO Auto-generated method stub
		//return null;
	}

	@Override
	protected Expr copy(ExprList args) {
		return new E_RdfTerm(args.get(0), args.get(1), args.get(2), args.get(3));
	}

}