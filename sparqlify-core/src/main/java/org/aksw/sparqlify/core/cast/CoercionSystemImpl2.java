package org.aksw.sparqlify.core.cast;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;

public class CoercionSystemImpl2
	implements CoercionSystem<TypeToken, NodeValueTransformer>
{
	private TypeSystem typeSystem;
	private Map<TypeToken, Map<TypeToken, NodeValueTransformer>> sourceToTargetToTransform = new HashMap<TypeToken, Map<TypeToken, NodeValueTransformer>>();

	public CoercionSystemImpl2(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public NodeValueTransformer lookup(TypeToken sourceTypeName, TypeToken targetTypeName) {
		Map<TypeToken, NodeValueTransformer> targetToDecl = sourceToTargetToTransform.get(sourceTypeName);
		if(targetToDecl == null) {
			return null;
		}
		
		NodeValueTransformer result = targetToDecl.get(targetTypeName);
		
		return result;
	}

	
	/**
	 * Registers a coercion.
	 * 
	 * 
	 * 
	 * @param sourceTypeName
	 * @param targetTypeName
	 */
	public void registerCoercion(TypeToken sourceTypeName, TypeToken targetTypeName, NodeValueTransformer transformer) {

		if(typeSystem.isSuperClassOf(targetTypeName, sourceTypeName)) {
			throw new RuntimeException("The return type must not be a subtype of the argument.");
		}
		
		Map<TypeToken, NodeValueTransformer> targetToDecl = sourceToTargetToTransform.get(sourceTypeName);
		NodeValueTransformer priorDecl = null;
		if(targetToDecl == null) {
			targetToDecl = new HashMap<TypeToken, NodeValueTransformer>();
			sourceToTargetToTransform.put(sourceTypeName, targetToDecl);
		} else {
			priorDecl = targetToDecl.get(targetTypeName);
		}

		
		//XClass priorTargetClass = targetToDecl.get(targetClass);
		
		if(priorDecl != null) {
			throw new RuntimeException("Coercion " + transformer + " from " + sourceTypeName + " to " + targetTypeName + " already defined with " + priorDecl);
		}

		targetToDecl.put(targetTypeName, transformer);
	}
	
}