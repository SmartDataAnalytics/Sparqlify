package org.aksw.sparqlify.sparqlview;

import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;

public interface View {
	String getName();
	
	Set<Var> getVarsMentioned();
	View copySubstitute(Map<Node, Node> renamer);
}
