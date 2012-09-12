package org.aksw.sparqlify.expr.util;

import java.math.BigDecimal;

import org.aksw.sparqlify.algebra.sparql.expr.NodeValueGeom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class NodeValueUtils {

	private static final Logger logger = LoggerFactory.getLogger(NodeValueUtils.class);
	
	public static Object getValue(NodeValue expr) {
		if(expr.isIRI()){
			logger.debug("HACK - Uri constants should be converted to RdfTerms first");
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