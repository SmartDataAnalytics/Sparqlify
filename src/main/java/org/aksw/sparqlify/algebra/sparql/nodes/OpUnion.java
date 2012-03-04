package org.aksw.sparqlify.algebra.sparql.nodes;

public class OpUnion
	extends OpBase2
{
	public OpUnion(Op left, Op right) {
		super(left, right);
	}

	@Override
	public String getName() {
		return "union";
	}	
}
