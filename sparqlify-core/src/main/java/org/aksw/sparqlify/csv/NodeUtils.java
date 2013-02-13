package org.aksw.sparqlify.csv;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.graph.Node;

public class NodeUtils {
	public static String toNTriplesString(Node node) {
		String result; 
		if(node.isURI()) {
			result = "<" + node.getURI() + ">";
		}
		else if(node.isLiteral()) {
			String lex = node.getLiteralLexicalForm();
			String lang = node.getLiteralLanguage();
			String dt = node.getLiteralDatatypeURI();
			
			String tmp = lex;
			// \\   \"   \n    \t   \r 
			tmp = tmp.replace("\\", "\\\\");
			tmp = tmp.replace("\"", "\\\"");
			tmp = tmp.replace("\n", "\\n");
			tmp = tmp.replace("\t", "\\t");
			tmp = tmp.replace("\r", "\\r");
			
			String encoded = tmp;
			// If fields contain new lines, escape them with triple quotes
//			String quote = encoded.contains("\n")
//					? "\"\"\""
//					: "\"";
			String quote = "\"";
			
			result =  quote + encoded + quote;
			
			if(!StringUtils.isEmpty(dt)) {
				result = result + "^^<" + dt+ ">";  
			} else {
				if(!lang.isEmpty()) {
					result = result + "@" + lang;
				}
			}			
		}
		else if(node.isBlank()) {
			result = node.getBlankNodeLabel();
		} else {
			throw new RuntimeException("Cannot serialize [" + node + "] as N-Triples");
		}
		
		return result;
	}
}