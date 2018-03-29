package org.aksw.r2rml.impl.jena;

import java.util.Optional;
import java.util.function.Function;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public abstract class AbstractR2rmlResourceImpl
	extends ResourceImpl
{
	public AbstractR2rmlResourceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	

	public static <T extends RDFNode> Optional<T> getObjectAs(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = Optional.ofNullable(s.getProperty(p))
				.map(Statement::getObject)
				.filter(o -> o.canAs(clazz))
				.map(o -> o.as(clazz));
		
		return result;
	}
	
	public static <T> Optional<T> getLiteralValue(Resource s, Property p, Function<? super Literal, ? extends T> fn) {
		Optional<T> result = Optional.ofNullable(s.getProperty(p))
				.map(Statement::getObject)
				.map(RDFNode::asLiteral)
				.map(fn);

		return result;
		
	}
	
	/**
	 * Placeholder function which may in the future only remove properties of the given type 
	 * 
	 * @param s
	 * @param p
	 * @param o
	 * @return
	 */
	public static <S extends Resource> S setLiteralValue(S s, Property p, Object o) {
		s.removeAll(p);
		if(o != null) {
			s.addLiteral(p, o);
		}
		return s;
	}

	public static <S extends Resource> S setProperty(S s, Property p, RDFNode o) {
		s.removeAll(p);
		s.addProperty(p, o);
		return s;
	}

}
