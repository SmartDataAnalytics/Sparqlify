package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.XSD;


/**
 * Checks whether xsd:numeric is a super class of the given expression
 * 
 * 
 * @author raven
 *
 */
public class ExprTransformerHasRdfTermType
	extends ExprTransformerBase1
{
	//private static final Logger logger = LoggerFactory.getLogger(ExprTransformerSparqlFunctionModel.class);
	

	int expectedRdfTermType;

	
	public ExprTransformerHasRdfTermType(int expectedRdfTermType) {
		this.expectedRdfTermType = expectedRdfTermType;
	}

	
	/**
	 * TODO: IsNumeric implies not null
	 * 
	 */
	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a) {

	    Expr type = a.getType();
	    if(!type.isConstant()) {
	        throw new RuntimeException("Dynamic RDF Term type not supported");
	    }
	    
	    String actualRdfTermType = type.getConstant().getNode().getLiteralLexicalForm();
	    
	    //NodeValue nv = type.getConstant();
	    //int actualRdfTermType = nv.getInteger().intValue();
	    
	    
	    boolean isEqual = actualRdfTermType.equals("" + expectedRdfTermType);
	    
	    E_RdfTerm result = isEqual ? E_RdfTerm.TRUE : E_RdfTerm.FALSE;

		return result;
	}
	
}
