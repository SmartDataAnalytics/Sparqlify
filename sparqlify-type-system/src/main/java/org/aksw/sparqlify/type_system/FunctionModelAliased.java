package org.aksw.sparqlify.type_system;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A function model that can assign multiple method declarations (each with their own name)
 * to a name.
 * 
 * 
 * For instance, it could map the name 'total' to the method
 * descriptor of 'int sum(int)'.
 * 
 * @author raven
 *
 * @param <T>
 */
public class FunctionModelAliased<T>
	implements FunctionModel<T>
{
	/**
	 * Maps a name to a set of method descriptors,
	 * effectively overriding the names of the underlying function model.
	 * 
	 * 
	 * 
	 * 
	 */
	private Multimap<String, String> nameToDescriptors;
	private FunctionModel<T> baseModel;

	
	
	public FunctionModelAliased(FunctionModel<T> baseModel) {
		this(baseModel, HashMultimap.<String, String>create());
	}

	public FunctionModelAliased(FunctionModel<T> baseModel, Multimap<String, String> nameToDescriptors) {
		this.baseModel = baseModel;
		this.nameToDescriptors = nameToDescriptors;
	}


	/**
	 * Find a method by its method descriptor
	 * 
	 * @param descriptor The method descriptor
	 */
	@Override
	public MethodEntry<T> lookupById(String descriptor) {
		MethodEntry<T> result = baseModel.lookupById(descriptor);
		return result;
	}

	// TODO This function might be too internal to expose in this interface
	@Override
	public Collection<CandidateMethod<T>> lookup(
			Collection<MethodEntry<T>> candidates, List<T> argTypes) {
		throw new RuntimeException("Dont use this");
	}

	@Override
	public Collection<CandidateMethod<T>> lookupByName(String methodName,
			List<T> argTypes)
	{
		// Map the name to the descriptors
		Collection<String> descriptors = nameToDescriptors.get(methodName);
		
		Collection<MethodEntry<T>> candidates = new HashSet<MethodEntry<T>>();
		for(String descriptor : descriptors) {
			MethodEntry<T> entry = baseModel.lookupById(descriptor);
			
			if(entry != null) {
				candidates.add(entry);
			}
		}
		
		Collection<CandidateMethod<T>> result = baseModel.lookup(candidates, argTypes);
		
		return result;
	}

	@Override
	public Collection<String> getIdsByName(String name) {
		Collection<String> result = nameToDescriptors.get(name);
		return result;
	}

	
	@Override
	public String getNameById(String id) {
		// TODO Optimize this lookup!
		String result = FunctionModelImpl.getFirstKey(nameToDescriptors, id);
		
		return result;
	}

	@Override
	public Collection<MethodEntry<T>> getMethodEntries() {
		Collection<MethodEntry<T>> result = baseModel.getMethodEntries();
		return result;
	}


	public MethodEntry<T> registerFunction(String name, MethodDeclaration<T> declaration) {
		MethodEntry<T> result = baseModel.registerFunction(declaration);
		
		nameToDescriptors.put(name, result.getId());
		
		return result;
	}


	@Override
	public MethodEntry<T> registerFunction(MethodDeclaration<T> declaration) {
		MethodEntry<T> result = baseModel.registerFunction(declaration);

		nameToDescriptors.put(declaration.getName(), result.getId());

		return result;
	}

	@Override
	public void registerCoercion(MethodDeclaration<T> declaration) {
		baseModel.registerCoercion(declaration);
		
		//throw new RuntimeException("Not implemented yet");
	}

	@Override
	@Deprecated
	public MethodEntry<T> registerFunction(String id, String name,
			MethodSignature<T> signature) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	@Deprecated
	public void registerCoercion(String id, String name, MethodSignature<T> signature) {
		throw new RuntimeException("Not implemented yet");
	}

	
	@Override
	public Map<String, String> getInverses() {
		throw new RuntimeException("Not implemented yet");
	}

}

