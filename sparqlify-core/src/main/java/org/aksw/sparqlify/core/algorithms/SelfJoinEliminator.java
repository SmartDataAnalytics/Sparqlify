package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.domain.ViewInstance;


public class SelfJoinEliminator {

	/**
	 * Does an merge of self-joins if possible.
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return null if no merge occured, or a new instance holding the merge
	 */
	public static ViewInstance merge(ViewInstance a, ViewInstance b)
	{
		return null;
	}
	/*
	static RdfViewInstance merge(ViewInstance a, ViewInstance b)
	{
		if(a.getViewDefinition() != b.getViewDefinition()) {
			return null;
		}
		
		SetMultimap<Var, Var> backA = HashMultimap.create(a.getParentToQueryBinding());
		SetMultimap<Var, Var> backB = HashMultimap.create(b.getParentToQueryBinding());

		//System.out.println("BackA: " + backA);
		//System.out.println("BackB: " + backB);

		// Now check if each parent variable in backA maps to the same
		// query variables as in backB
		// If that is the case, we have a self join
		for(Var varA : backA.keySet()) {
		
			Set<Var> varsA = backA.get(varA);
			Set<Var> varsB = backB.get(varA);
			
			if(varsB.isEmpty()) {
				continue;
			}
			
			
			if(Sets.intersection(varsA, varsB).isEmpty()) {
				//System.out.println("RESULT: No self join");
				
				return null;
			}
		}
		
		
		//Set valsA = a.getBinding().getEquiMap().getKeyToValue();
		// TODO URGENT: Above check is not sufficient
		// I think we need to check whether the key-to-value entries map to the same thing
		for(Entry<Var, Node> entry : a.getBinding().getEquiMap().getKeyToValue().entrySet()) {
			Var parentVar = (Var)a.getRenamer().inverse().get(entry.getKey());
			
			Var bVar = (Var)b.getRenamer().get(parentVar);
			Node bValue = b.getBinding().getEquiMap().getKeyToValue().get(bVar);
			
			if(bValue != null && !entry.getValue().equals(bValue)) {
				return null;
			}
		}

		
		//System.out.println("RESULT: Self join");
		
		
		// Create a copy of a (this is needed because we might be trying
		// out whether the cartesian product of view conjunctions is satisfiable, therefore
		// we must not change the state of the product)
		ViewInstance result = a.copy();
		
		// Now we have a self join. This means we have to express the binding
		// of the second view instance in terms of the first one
		
		
		// Add to 'a' all query patterns of 'b'
		//result.getQueryQuads().addAll(b.getQueryQuads());
		//result.getViewQuads().addAll(b.getViewQuads());

		// Replace in the binding of b all variables with those of a
		VarBinding mergedBinding = new VarBinding();
		mergedBinding.addAll(b.getBinding());
		
		
		// Map the names of b backt to the parent, and from the parent back to a
		Map<Node, Node> mergeMap = MapUtils.createChainMap(b.getRenamer().inverse(), result.getRenamer());
		mergedBinding = mergedBinding.copySubstitute(mergeMap);
		
		result.getBinding().addAll(mergedBinding);

		/*
		System.out.println("------------------------------------------------------");
		System.out.println("a: " + a.getBinding());
		System.out.println("b: " + b.getBinding());

		System.out.println("MERGE RESULT: " + result.getBinding());
		* /
		//System.out.println("MERGE RESULT: " + result.getQueryQuads());
		
		return result;
	}
	*/
	
	/**
	 * Eleminate self-joins from the conjunction
	 * In place operation
	 * 
	 * @param conjunction
	 * @return
	 */
	public static void merge(ViewInstanceJoin conjunction) {
		
		for(int i = 0; i < conjunction.getViewInstances().size(); ++i) {
			ViewInstance a = conjunction.getViewInstances().get(i);
			
			for(int j = i + 1; j < conjunction.getViewInstances().size(); ++j) {
				ViewInstance b = conjunction.getViewInstances().get(j);
				
				ViewInstance view = merge(a, b);
				if(view != null) { // FIXME Assumes that b is always merged into a (maybe in the future it may change)
					a = view;
					conjunction.getViewInstances().set(i, view);
					conjunction.getViewInstances().remove(j);
					--j;
				}

			}
		}
	}

}
