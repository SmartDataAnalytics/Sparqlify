package org.aksw.sparqlify.jpa;

import java.util.List;

import org.apache.jena.sparql.core.Quad;

public interface EntityInverseMapper {
	public List<EntityRef> map(Quad quad);
}
