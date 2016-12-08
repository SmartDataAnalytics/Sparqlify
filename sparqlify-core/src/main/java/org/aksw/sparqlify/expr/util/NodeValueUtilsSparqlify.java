package org.aksw.sparqlify.expr.util;

import org.aksw.jena_sparql_api.exprs_ext.NodeValueGeom;
import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeValueUtilsSparqlify {
    public static Object getValue(NodeValue expr) {
        Object result;
        if(expr instanceof NodeValueGeom){
            result = ((NodeValueGeom) expr).getGeometry();
        } else {
            result = NodeValueUtils.getValue(expr);
        }

        return result;
    }

}
