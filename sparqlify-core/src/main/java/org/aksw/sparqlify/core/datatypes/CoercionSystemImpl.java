package org.aksw.sparqlify.core.datatypes;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

/**
 * A function can at the same time be registered for the coercion.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
class CoercionSystemImpl
	implements CoercionSystem
{
	
	private Map<XClass, Map<XClass, XMethod>> sourceToTargetToDecl = new HashMap<XClass, Map<XClass, XMethod>>();
	
	public void register(XMethod decl) {
		MethodSignature<XClass> signature = null; // decs.getTypeSignature
		
		// Require one argument, and the return type must not be sub-type of the argument
		if(signature.getParameterTypes().size() != 1) {
			throw new RuntimeException("Only 1 argument allowed for implicit conversions");
		}
		
		XClass source = signature.getParameterTypes().get(0);
		XClass target = signature.getReturnType();
		
		if(source.isAssignableFrom(target)) {
			throw new RuntimeException("The return type must not be a subtype of the argument.");
		}
		
		Map<XClass, XMethod> targetToDecl = sourceToTargetToDecl.get(source);
		XMethod priorDecl = null;
		if(targetToDecl == null) {
			targetToDecl = new HashMap<XClass, XMethod>();
			sourceToTargetToDecl.put(source, targetToDecl);
		} else {
			priorDecl = targetToDecl.get(target);
		}

		
		//XClass priorTargetClass = targetToDecl.get(targetClass);
		
		if(priorDecl != null) {
			throw new RuntimeException("Coercion " + decl + " from " + source + " to " + target + " already defined with " + priorDecl);
		}

		targetToDecl.put(target, decl);
	}
	
	public XMethod lookup(XClass source, XClass target) {
		Map<XClass, XMethod> targetToDecl = sourceToTargetToDecl.get(source);
		if(targetToDecl == null) {
			return null;
		}
		
		XMethod result = targetToDecl.get(target);
		
		return result;
	}
	
}
