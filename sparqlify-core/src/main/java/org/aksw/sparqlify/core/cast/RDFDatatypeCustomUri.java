package org.aksw.sparqlify.core.cast;

import com.hp.hpl.jena.datatypes.RDFDatatype;

public class RDFDatatypeCustomUri
	extends RDFDatatypeDecorator
{
	private String uri;
	
	public RDFDatatypeCustomUri(String uri, RDFDatatype rdfDatatype) {
		super(rdfDatatype);
		this.uri = uri;
	}
	
	@Override
	public String getURI() {
		return uri;
	}
	
	@Override
	public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
		RDFDatatype tmp = super.normalizeSubType(value, dt);
		
		RDFDatatype result =  new RDFDatatypeCustomUri(uri, tmp);
		return result;
	}

	
	
//	public static RDFDatatypeCustomUri create(String uri, RDFDatatype rdfDatatype) {
//		return new RDFDatatypeCustomUri(uri, rdfDatatype);
//	}
}