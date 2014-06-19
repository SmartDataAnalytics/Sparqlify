package org.aksw.sparqlify.algebra.sparql.expr;

import java.util.List;

import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.jena.functions.RdfTerm;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.XSD;


public class E_RdfTerm
	extends ExprFunctionN
{
	public static final NodeValue typeVar = NodeValue.makeInteger(-1);
	public static final NodeValue typeBlank = NodeValue.makeInteger(0);
	public static final NodeValue typeUri = NodeValue.makeInteger(1);
	public static final NodeValue typePlainLiteral = NodeValue.makeInteger(2);
	public static final NodeValue typeTypedLiteral = NodeValue.makeInteger(3);


	public static final E_RdfTerm TRUE = E_RdfTerm.createTypedLiteral(NodeValue.TRUE, XSD.xboolean);
	public static final E_RdfTerm FALSE = E_RdfTerm.createTypedLiteral(NodeValue.FALSE, XSD.xboolean);
	public static final E_RdfTerm TYPE_ERROR = SqlTranslationUtils.expandConstant(SparqlifyConstants.nvTypeError);
	

	public static E_RdfTerm createVar(ExprVar expr) {
		return new E_RdfTerm(typeVar, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
	}
	
	public static E_RdfTerm createBlankNode(Expr expr) {
		return new E_RdfTerm(typeBlank, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
	}
	
	public static E_RdfTerm createUri(Expr expr) {
		return new E_RdfTerm(typeUri, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
	}

	public static E_RdfTerm createPlainLiteral(Expr expr) {
		return new E_RdfTerm(typePlainLiteral, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
	}
	
	public static E_RdfTerm createTypedLiteral(Expr expr, Expr datatype) {
		
		//DatatypeSystemDefault.
		return new E_RdfTerm(typeTypedLiteral, expr, NodeValue.nvEmptyString, datatype);
	}

	public static E_RdfTerm createTypedLiteral(Expr expr, Resource datatype) {

		return createTypedLiteral(expr, datatype.asNode());
	}

	public static E_RdfTerm createTypedLiteral(Expr expr, Node datatype) {
		
		String datatypeUri = datatype.getURI();
		Expr datatypeExpr = NodeValue.makeString(datatypeUri);
		//DatatypeSystemDefault.
		return new E_RdfTerm(typeTypedLiteral, expr, NodeValue.nvEmptyString, datatypeExpr);
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
    public NodeValue eval(List<NodeValue> args) {
		return RdfTerm.eval(args.get(0), args.get(1), args.get(2), args.get(3));
		//RdfTerm
		//throw new RuntimeException("Should not happen");
		// TODO Auto-generated method stub
		//return null;
	}

	@Override
    public Expr copy(ExprList args) {
		return new E_RdfTerm(args.get(0), args.get(1), args.get(2), args.get(3));
	}

}