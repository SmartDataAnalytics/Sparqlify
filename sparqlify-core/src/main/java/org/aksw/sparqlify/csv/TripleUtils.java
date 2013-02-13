package org.aksw.sparqlify.csv;

import com.hp.hpl.jena.graph.Triple;

public class TripleUtils {
	public static String toNTripleString(Triple triple) {
		String s = NodeUtils.toNTriplesString(triple.getSubject());
		String p = NodeUtils.toNTriplesString(triple.getPredicate());
		String o = NodeUtils.toNTriplesString(triple.getObject());
		
		String result = s + " " + p + " " + o + " .";
		
		return result;
	}
}