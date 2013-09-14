package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.type_system.TypeModel;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * This function does not exist in SPARQL.
 * Its just there for experiments. Use isNumeric instead.
 * 
 * 
 * @author raven
 *
 */
public class ET_IsDecimal
	extends ExprTransformerBase1
{
	private TypeModel<String> typeModel;

	public ET_IsDecimal(TypeModel<String> typeModel) {
		this.typeModel = typeModel;
	}

	
	/**
	 * TODO: IsNumeric implies not null
	 * 
	 */
	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a) {

		String subType = a.getDatatype().getConstant().asUnquotedString();

		boolean isDecimal = typeModel.isSuperTypeOf(XSD.decimal.toString(), subType);
		
		E_RdfTerm result;
		if(isDecimal) {
			result = E_RdfTerm.TRUE;
		}
		else {
			result = E_RdfTerm.FALSE;
		}
		
		return result;
	}
	
}
