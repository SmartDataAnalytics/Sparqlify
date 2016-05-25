package org.aksw.sparqlify.views.transform;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.core.RdfViewInstance;
import org.aksw.sparqlify.core.algorithms.OpMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

public class GetVarsMentioned {
    @SuppressWarnings("unchecked")
    public static Set<Var> getVarsMentioned(Op op) {
        return (Set<Var>)MultiMethod.invokeStatic(GetVarsMentioned.class, "_getVarsMentioned", op);
    }

    @Deprecated
    public static Set<Var> _getVarsMentioned(RdfViewInstance op) {
        return new HashSet<Var>(op.getQueryToParentBinding().keySet());
    }


    public static Set<Var> _getVarsMentioned(OpMapping op) {
        Set<Var> result = new HashSet<Var>(op.getMapping().getVarDefinition().getMap().keySet());

        return result;
    }


}