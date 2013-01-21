package org.aksw.sparqlify.core.cast;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;

public class CoercionSystemImpl3
	implements CoercionSystem<TypeToken, SqlValueTransformer>
{
	private TypeSystem typeSystem;
	private Map<TypeToken, Map<TypeToken, SqlValueTransformer>> sourceToTargetToTransform = new HashMap<TypeToken, Map<TypeToken, SqlValueTransformer>>();

	public CoercionSystemImpl3(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public SqlValueTransformer lookup(TypeToken sourceTypeName, TypeToken targetTypeName) {
		Map<TypeToken, SqlValueTransformer> targetToDecl = sourceToTargetToTransform.get(sourceTypeName);
		if(targetToDecl == null) {
			return null;
		}
		
		SqlValueTransformer result = targetToDecl.get(targetTypeName);
		
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
	public void registerCoercion(TypeToken sourceTypeName, TypeToken targetTypeName, SqlValueTransformer transformer) {

		if(typeSystem.isSuperClassOf(targetTypeName, sourceTypeName)) {
			throw new RuntimeException("The return type must not be a subtype of the argument.");
		}
		
		Map<TypeToken, SqlValueTransformer> targetToDecl = sourceToTargetToTransform.get(sourceTypeName);
		SqlValueTransformer priorDecl = null;
		if(targetToDecl == null) {
			targetToDecl = new HashMap<TypeToken, SqlValueTransformer>();
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