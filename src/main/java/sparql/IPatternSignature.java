package sparql;

import java.util.Set;

import com.hp.hpl.jena.graph.Node;

/**
 * TODO Rename everything related to IPatternType to IPatternSignature
 * 
 * @author raven
 *
 */
public interface IPatternSignature
{
	Set<Node> getVarsMentioned();
}