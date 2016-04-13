package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.core.domain.input.Mapping;

import org.apache.jena.sparql.algebra.Op;

/**
 * Interface for rewriters
 * for algebraic expressions based on view instances.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface OpMappingRewriter {
	Mapping rewrite(Op op);
}
