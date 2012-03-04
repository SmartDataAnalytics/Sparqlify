package org.aksw.sparqlify.core;

import mapping.SparqlifyConstants;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.karneim.util.collection.regex.PatternPro;

public class RdfTermPatternDerivation {
	public static RdfTermPattern deriveRegex(Expr expr) {
		return MultiMethod.invokeStatic(RdfTermPatternDerivation.class, "_deriveRegex",
				expr);
	}
	
	public static RdfTermPattern _deriveRegex(NodeValue expr) {
		
		Node node = expr.asNode();
		
		String value = null;
		if(node.isLiteral()) {
			value = node.getLiteralLexicalForm();
		} else if(node.isURI()) {
			value = node.getURI();
		} else {
			throw new RuntimeException("Should not happen");
		}
		
		String datatype = null;
		if(node.isLiteral()) {
			datatype = node.getLiteralDatatypeURI();
		}
		
		PatternPro a = value == null ? null : new PatternPro(RegexUtils.escape(value));
		PatternPro b = datatype == null ? null : new PatternPro(RegexUtils.escape(datatype));
		
		return new RdfTermPattern(a, b);
	}

	public static RdfTermPattern _deriveRegex(E_Function expr) {
		if(SparqlifyConstants.rdfTermLabel.equals(expr.getFunctionIRI())) {
			return new RdfTermPattern(
					new PatternPro(RegexDerivation.deriveRegex(expr.getArg(2))));
			
		}
		
		return null;
	}

	
	public static RdfTermPattern _deriveRegex(E_RdfTerm expr) {
		return new RdfTermPattern(
				new PatternPro(RegexDerivation.deriveRegex(expr.getLexicalValue()))
				//RegexDerivation.deriveRegex(expr.getDatatype()),
				);
	}
	
}