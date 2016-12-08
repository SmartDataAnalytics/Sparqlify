package org.aksw.sparqlify.core.sparql.algebra.transform;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.core.algorithms.OpMapping;
import org.aksw.sparqlify.database.FilterPlacementOptimizer2;
import org.apache.jena.sparql.algebra.Op;

public class FilterPlacementOptimizer2Sparqlify
    extends FilterPlacementOptimizer2
{
    private static FilterPlacementOptimizer2Sparqlify instance;

    public static FilterPlacementOptimizer2Sparqlify get() {
        if(instance == null) {
            instance = new FilterPlacementOptimizer2Sparqlify();
        }
        return instance;
    }


    public static Op optimizeStatic(Op op) {
        Op result = optimizeStatic(op, null);
        return result;
//        RestrictionManagerImpl cnf = new RestrictionManagerImpl();
//
//        FilterPlacementOptimizer2Sparqlify x = get();
//        //Op result = MultiMethod.invoke(FilterPlacementOptimizer2.class, "_optimize", op, cnf);
//        Op result = MultiMethod.invoke(x, "_optimize", op, cnf);
//        return result;
    }

    public static Op optimizeStatic(Op op, RestrictionManagerImpl cnf) {
        if(cnf == null) {
            cnf = new RestrictionManagerImpl();
        }

        FilterPlacementOptimizer2Sparqlify x = get();
        Op result = x.optimize(op,  cnf);
        //Op result = MultiMethod.invoke(x, "_optimize", op, cnf);
        return result;
    }


    public Op _optimize(OpMapping op, RestrictionManagerImpl cnf) {
        return surroundWithFilterIfNeccessary(op, cnf);
    }

}
