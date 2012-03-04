package sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.jena.util.QuadUtils;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.Quad;

public class PatternUtils
{

	public static Collection<Quad> collectQuads(Op op) {
		return collectQuads(op, new HashSet<Quad>());
	}

	public static Collection<Quad> collectQuads(Op op, Collection<Quad> result) {
		if(op instanceof OpLeftJoin) {
			OpLeftJoin x = (OpLeftJoin)op;
			collectQuads(x.getLeft(), result);
			collectQuads(x.getRight(), result);
		} else if(op instanceof OpFilter) {
			OpFilter x = (OpFilter)op;
			collectQuads(x.getSubOp(), result);
		} else if(op instanceof OpJoin) {
			OpJoin x = (OpJoin)op;
			
			collectQuads(x.getLeft(), result);
			collectQuads(x.getRight(), result);
		} else if(op instanceof OpUnion) {
			System.out.println("Warning: Collecting expressions from unions. Since the same vars may appear within different (parts of) unions, it may be ambiguous to which part the expression refers.");
	
			OpUnion x = (OpUnion)op;
	
			collectQuads(x.getLeft(), result);
			collectQuads(x.getRight(), result);
		} else if(op instanceof OpQuadPattern) {
			OpQuadPattern x = (OpQuadPattern)op;
			result.addAll(x.getPattern().getList());			
		} else if(op instanceof OpSequence) {
			OpSequence x = (OpSequence)op;
			for(Op element : x.getElements()) {
				collectQuads(element, result);
			}			
		} else {
			throw new NotImplementedException("Encountered class: " + op);
		}
		
		return result;
	}


	
	// Replaced by getVarsMentioned
	@Deprecated
	public static Set<Node> getVariables(Iterable<Node> nodes)
	{
		Set<Node> result = new HashSet<Node>();
		for (Node node : nodes) {
			if (node.isVariable()) {
				result.add(node);
			}
		}
	
		return result;
	}

	// Replaced by getVarsMentioned
	@Deprecated
	public static Set<Node> getVariables(Quad quad)
	{
		return getVariables(QuadUtils.quadToList(quad));
	}

}
