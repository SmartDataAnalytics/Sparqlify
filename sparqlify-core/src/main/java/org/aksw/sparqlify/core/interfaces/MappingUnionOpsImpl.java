package org.aksw.sparqlify.core.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.MappingUnion;
import org.aksw.sparqlify.core.domain.input.VarDefinition;

import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprList;


/**
 * TODO Work in progress...
 * The question is, whether it is sufficient to only have a MappingUnion structure, or whether we should go for
 * an analog to the ElementTreeAnalyzer
 * 
 * Main motivation for this approach is group by / aggregation: When we encounter group by node, we may remove mappings
 * which yield a type error with the aggregate function e.g. if such members would result in computing the sum of strings.
 * 
 * @author raven
 *
 */
public class MappingUnionOpsImpl
    implements MappingUnionOps
{
    private MappingOps mappingOps;


    @Override
    public MappingUnion rename(MappingUnion a, Map<String, String> columnRenames) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion join(MappingUnion a, Mapping b) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion leftJoin(MappingUnion a, Mapping b) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion union(List<MappingUnion> members) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion slice(MappingUnion a, Long limit, Long offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion project(MappingUnion a, List<Var> vars) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @Override
    public MappingUnion filter(MappingUnion as, ExprList exprs) {        
        List<Mapping> ms = new ArrayList<Mapping>(as.getMappings().size());
        for(Mapping a : as.getMappings()) {
            Mapping m = mappingOps.filter(a, exprs);
            if(!m.isEmpty()) {
                ms.add(m);
            }
        }
        
        MappingUnion result = new MappingUnion(ms);
        return result;
    }

    
    @Override
    public MappingUnion distinct(MappingUnion a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion groupBy(MappingUnion a, VarExprList groupVars,
            List<ExprAggregator> aggregators) {
        //mappingOps.groupBy(a, groupVars, aggregators)
        return null;
    }

    @Override
    public MappingUnion extend(MappingUnion a, VarDefinition varDef) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MappingUnion order(MappingUnion a, List<SortCondition> sortConditions) {
        // TODO Auto-generated method stub
        return null;
    }

}
