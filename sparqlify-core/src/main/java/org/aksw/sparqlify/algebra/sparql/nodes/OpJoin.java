package org.aksw.sparqlify.algebra.sparql.nodes;


public class OpJoin
	extends OpBase2
{
	public OpJoin(Op left, Op right) {
		super(left, right);
	}

	@Override
	public String getName() {
		return "join";
	}	
}
