package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.MethodSignature;

public class MethodDeclarationParserSimple {
	
	/**
	 * Very hacky parser for method declarations
	 * TODO Use a grammar based parser instead
	 * 
	 * @param str
	 * @return
	 */
	public static MethodDeclaration<String> parse(String str) {
		String[] splits = str.replace('(', ' ').replace(')', ' ').replace(',', ' ').trim().split("\\s+");
		//String[] splits = str.split("\\s*(,|\\(|\\))");
		
		String returnType = splits[0];
		String name = splits[1];
		
		List<String> paramTypes = new ArrayList<String>();
		for(int i = 2; i < splits.length - 1; ++i) {
			String type = splits[i];
			
			paramTypes.add(type);
		}
		
		String last = splits[splits.length - 1];
		
		String varArgType = null;
		if(last.endsWith("...")) {
			varArgType = last.split("\\s+", 2)[0];
		} else {
			paramTypes.add(last);
		}
		
		MethodSignature<String> sig = MethodSignature.create(returnType, paramTypes, varArgType);
		MethodDeclaration<String> result = MethodDeclaration.create(name, sig);
		return result;
	}
	
	
}