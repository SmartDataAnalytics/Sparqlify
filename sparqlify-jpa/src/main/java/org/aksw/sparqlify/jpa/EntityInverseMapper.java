package org.aksw.sparqlify.jpa;

import com.hp.hpl.jena.sparql.core.Quad;

public interface EntityInverseMapper {
	public EntityRef map(Quad quad);
}
