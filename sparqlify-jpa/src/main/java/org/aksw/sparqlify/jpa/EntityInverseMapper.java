package org.aksw.sparqlify.jpa;

import java.util.List;

import com.hp.hpl.jena.sparql.core.Quad;

public interface EntityInverseMapper {
	public List<EntityRef> map(Quad quad);
}
