package org.aksw.sparqlify.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.util.NodeComparator;
import com.hp.hpl.jena.sparql.util.TripleComparator;

public class QuadPatternUtils {
	public static QuadPattern toQuadPattern(BasicPattern basicPattern) {
		return toQuadPattern(Quad.defaultGraphNodeGenerated, basicPattern);
	}
	
	// This method is implictely part of OpQuadPattern, but its not reusable yet
	public static QuadPattern toQuadPattern(Node g, BasicPattern basicPattern) {
		
		QuadPattern result = new QuadPattern();
		for(Triple triple : basicPattern) {
			Quad quad = new Quad(g, triple);
			result.add(quad);
		}
		
		return result;
	}
	
	/**
	 * Creates a set of triples by omitting the graph node of the quads
	 * 
	 * @param quadPattern
	 * @return
	 */
	public static BasicPattern toBasicPattern(QuadPattern quadPattern)
	{
		BasicPattern result = new BasicPattern();

		for(Quad quad : quadPattern) {
			Triple triple = quad.asTriple();
			result.add(triple);
		}
		
		return result;
	}
	
	
	public static Map<Node, Set<Triple>> indexSorted(Iterable<Quad> quads) 
	{
		Map<Node, Set<Triple>> result = new TreeMap<Node, Set<Triple>>(new NodeComparator());
		for(Quad q : quads) {
			Set<Triple> triples = result.get(q.getGraph());
			if(triples == null) {
				triples = new TreeSet<Triple>(new TripleComparator());
				result.put(q.getGraph(), triples);
			}
			
			triples.add(q.asTriple());
		}
		
		return result;		
	}

	public static Map<Node, Graph> indexAsGraphs(Iterable<Quad> quads) {
		Map<Node, Graph> result = new HashMap<Node, Graph>();
		for(Quad q : quads) {
			Graph graph = result.get(q.getGraph());
			if(graph == null) {
				graph = GraphFactory.createDefaultGraph();
				result.put(q.getGraph(), graph);
			}
			
			graph.add(q.asTriple());
		}
		
		return result;
	}

}
