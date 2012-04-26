package org.aksw.sparqlify.expr.util;

import java.math.BigDecimal;

import org.aksw.sparqlify.algebra.sparql.expr.NodeValueGeom;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class NodeValueUtils {

	public static Object getValue(NodeValue expr) {
		if(expr.isIRI()){
			System.err.println("WARNING: HACK USED - Uri constants should be converted to RdfTerms first");
			return expr.asNode().getURI();
		} else if(expr.isBoolean()) {
			return expr.getBoolean();
		} else if(expr.isNumber()) {
			if(expr.isDecimal()) {
				BigDecimal d = expr.getDecimal();
				if(d.scale() > 0) {
					return d.doubleValue();
				} else {
					return d.intValue();
				}
			}
			else if(expr.isDouble()) {
				return expr.getDouble();	
			} else if(expr.isFloat()) {
				return expr.getFloat();
			} else {
				return expr.getDecimal().longValue();
			}
		} else if(expr.isString()) {
			return expr.getString();
		} else if(expr instanceof NodeValueGeom){
			return ((NodeValueGeom) expr).getGeometry();
		} else {
			throw new RuntimeException("Unknow datatype of contsant");
		}
	}
	
}