package sparql;

import java.util.Set;

import org.apache.jena.graph.Node;

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