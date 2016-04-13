package org.aksw.sparqlify.sparqlview;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public interface View {
	String getName();
	
	Set<Var> getVarsMentioned();
	View copySubstitute(Map<Node, Node> renamer);
}
