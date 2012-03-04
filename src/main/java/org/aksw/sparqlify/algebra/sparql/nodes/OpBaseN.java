package org.aksw.sparqlify.algebra.sparql.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;


public class OpBaseN {
	private List<Op> elements = new ArrayList<Op>();

	protected OpBaseN() {
		elements = new ArrayList<Op>();
	}

	protected OpBaseN(List<Op> x) {
		elements = x;
	}

	public void add(Op op) {
		elements.add(op);
	}

	public Op get(int idx) {
		return elements.get(idx);
	}

	// Tests the sub-elements for equalTo.
	/*
	protected boolean equalsSubOps(OpBaseN op, NodeIsomorphismMap labelMap) {
		Iterator<Op> iter1 = elements.listIterator();
		Iterator<Op> iter2 = op.elements.listIterator();

		for (; iter1.hasNext();) {
			Op op1 = iter1.next();
			Op op2 = iter2.next();
			if (!op1.equalTo(op2, labelMap))
				return false;
		}
		return true;
	}*/

	public int size() {
		return elements.size();
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	public List<Op> getElements() {
		return elements;
	}

	public Iterator<Op> iterator() {
		return elements.iterator();
	}

}
