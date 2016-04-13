package org.aksw.sparqlify.core.interfaces;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.algorithms.ViewInstance;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;

/**
 * Interface for the mapping algebra operations.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface MappingOps {
	
	Mapping createMapping(ViewInstance<ViewDefinition> viewInstance);
	
	/**
	 * Creates a new mapping with all column references
	 * in the variable definition renamed, and injects an
	 * approprate SqlProjection into the logical table
	 */
	Mapping rename(Mapping a, Map<String, String> columnRenames);
	
	Mapping join(Mapping a, Mapping b);
	Mapping leftJoin(Mapping a, Mapping b, ExprList exprs);
	
	// A binary union would be sucky to compute (permanently moving projections around)
	// Therfore we use one that deals with lists.
	Mapping union(List<Mapping> members);
	
	
	/**
	 * 
	 * 
	 * @param a
	 * @param limit The limit. Null for no limit
	 * @param offset The offset. Null for no offset
	 * @return
	 */
	Mapping slice(Mapping a, Long limit, Long offset);
	
	Mapping project(Mapping a, List<Var> vars);
	
	Mapping filter(Mapping a, ExprList exprs);
	
	Mapping distinct(Mapping a);

	Mapping groupBy(Mapping a, VarExprList groupVars, List<ExprAggregator> aggregators);

	Mapping extend(Mapping a, VarDefinition varDef);
	
	Mapping order(Mapping a, List<SortCondition> sortConditions);
}
