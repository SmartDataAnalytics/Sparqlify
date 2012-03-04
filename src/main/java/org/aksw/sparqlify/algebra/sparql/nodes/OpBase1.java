package org.aksw.sparqlify.algebra.sparql.nodes;

public abstract class OpBase1
	extends OpBase
{
	private Op sub;

	public OpBase1(Op subOp) {
		this.sub = subOp;
	}

	public Op getSubOp() {
		return sub;
	}
}
