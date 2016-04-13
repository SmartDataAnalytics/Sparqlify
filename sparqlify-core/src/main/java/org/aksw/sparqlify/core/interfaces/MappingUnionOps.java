package org.aksw.sparqlify.core.interfaces;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.algorithms.ViewInstance;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.MappingUnion;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;

public interface MappingUnionOps {
    
    //Mapping createMapping(ViewInstance<ViewDefinition> viewInstance);
    
    
    /**
     * Creates a new mapping with all column references
     * in the variable definition renamed, and injects an
     * approprate SqlProjection into the logical table
     */
    MappingUnion rename(MappingUnion a, Map<String, String> columnRenames);
    
    MappingUnion join(MappingUnion a, Mapping b);
    MappingUnion leftJoin(MappingUnion a, Mapping b);
    
    // A binary union would be sucky to compute (permanently moving projections around)
    // Therfore we use one that deals with lists.
    MappingUnion union(List<MappingUnion> members);
    
    
    /**
     * 
     * 
     * @param a
     * @param limit The limit. Null for no limit
     * @param offset The offset. Null for no offset
     * @return
     */
    MappingUnion slice(MappingUnion a, Long limit, Long offset);
    
    MappingUnion project(MappingUnion a, List<Var> vars);
    
    MappingUnion filter(MappingUnion a, ExprList exprs);
    
    MappingUnion distinct(MappingUnion a);

    MappingUnion groupBy(MappingUnion a, VarExprList groupVars, List<ExprAggregator> aggregators);

    MappingUnion extend(MappingUnion a, VarDefinition varDef);
    
    MappingUnion order(MappingUnion a, List<SortCondition> sortConditions);

}
