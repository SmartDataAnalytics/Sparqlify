package org.aksw.sparqlify.core.interfaces;

import java.util.Map;

import org.aksw.sparqlify.core.domain.Mapping;
import org.aksw.sparqlify.core.domain.ViewInstance;

/**
 * Interface for the mapping algebra operations. 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface MappingOps {
	Mapping createMapping(ViewInstance viewInstance);
	
	/**
	 * Creates a new mapping with all column references
	 * in the variable definition renamed, and injects an
	 * approprate SqlProjection into the logical table
	 */
	Mapping rename(Mapping a, Map<String, String> columnRenames);
	
	Mapping join(Mapping a, Mapping b);
	Mapping leftJoin(Mapping a, Mapping b);
	Mapping union(Mapping a, Mapping b);
}
