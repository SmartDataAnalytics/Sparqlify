package org.aksw.sparqlify.core.cast;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;

/**
 * FIXME WORKAROUND for Jena feature/bug
 * 
 * An RDF dataype wrapper that actually allows custom URIs for known datatypes
 * such as my:int for xsd:int
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RDFDatatypeDecorator
	implements RDFDatatype
{
	private RDFDatatype decoratee;
	
	public RDFDatatypeDecorator(RDFDatatype decoratee) {
		this.decoratee = decoratee;
	}
	
	@Override
	public String getURI() {
		return decoratee.getURI();
	}

	@Override
	public String unparse(Object value) {
		return decoratee.unparse(value);
	}

	@Override
	public Object parse(String lexicalForm) throws DatatypeFormatException {
		return decoratee.parse(lexicalForm);
	}

	@Override
	public boolean isValid(String lexicalForm) {
		return decoratee.isValid(lexicalForm);
	}

	@Override
	public boolean isValidValue(Object valueForm) {
		return decoratee.isValidValue(valueForm);
	}

	@Override
	public boolean isValidLiteral(LiteralLabel lit) {
		return decoratee.isValidLiteral(lit);
	}

//	public static RDFDatatype undecorate(RDFDatatype dt) {
//		
//	}
	
	@Override
	public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
		return decoratee.isEqual(value1, value2);
	}

	@Override
	public int getHashCode(LiteralLabel lit) {
		return decoratee.getHashCode(lit);
	}

	@Override
	public Class<?> getJavaClass() {
		return decoratee.getJavaClass();
	}

	@Override
	public Object cannonicalise(Object value) {
		return decoratee.cannonicalise(value);
	}

	@Override
	public Object extendedTypeDefinition() {
		return decoratee.extendedTypeDefinition();
	}

	@Override
	public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
		return decoratee.normalizeSubType(value, dt);
	}
	
}