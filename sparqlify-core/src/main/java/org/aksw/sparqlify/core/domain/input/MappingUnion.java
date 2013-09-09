package org.aksw.sparqlify.core.domain.input;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;


/**
 * Interface for nodes in the mapping hierarchy
 * 
 * @author raven
 *
 */
interface MappingMember {
	Mapping asSingle();
	MappingUnion asUnion();
}


/**
 * Leaf nodes in the tree
 * @author raven
 *
 */
class MappingMemberAtomic {
	private Mapping mapping;
}


abstract class MappingMemberUnionBase
	implements MappingMember
{

	@Override
	public Mapping asSingle() {
		throw new RuntimeException("This is the wrong sub class");
	}
}

/**
 * A union of mappings.
 * Supports (sub)grouping and ordering.
 * 
 * 
 * 
 * 
 * @author raven
 *
 */
public class MappingUnion
	extends MappingMemberUnionBase
	implements Indentable
{
	//private int order; // < 0: desc, 0: no order, > 0 asc 
	//private NavigableMap<Object, MappingMember> keyToMember; 
	//private List<K> memberOrder = null;
	private List<Mapping> mappings = new ArrayList<Mapping>();
	
	
	@Override
	public MappingUnion asUnion() {
		return this;
	}

	public List<Mapping> getMappings() {
		return mappings;
	}
	
	public void add(Mapping mapping) {
		this.mappings.add(mapping);
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.println("--- Begin of Mappings ---");
		for(int i = 0; i < mappings.size(); ++i) {
			Mapping mapping = mappings.get(i);
		
			writer.println("#" + i);
			writer.incIndent();
			mapping.getVarDefinition().toPrettyString();
			writer.decIndent();
		}
		writer.println("--- End of Mappings ---");
	}

	@Override
	public String toString() {
		return IndentableUtils.toString(this);
	}
	
}
