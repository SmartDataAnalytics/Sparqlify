package org.aksw.sparqlify.validation;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;


public class Validation {

	@Deprecated
	public static void validateView(RdfView view, Logger logger) {
		Set<Var> patternVars = QuadUtils.getVarsMentioned(view.getQuadPattern());
		
		Set<Var> bindingVars = new HashSet<Var>();
		for(Node bindingVar : view.getBinding().keySet()) {
			bindingVars.add((Var)bindingVar);
		}
		
		Set<Var> unboundPatternVars = Sets.difference(patternVars, bindingVars);
		
		if(!unboundPatternVars.isEmpty()) {
			//throw new RuntimeException("View " + view.getName() + ": Unbound pattern variables: " + unboundPatternVars);
			logger.error("View " + view.getName() + ": Unbound pattern variables: " + unboundPatternVars);
		}

		
		Set<Var> unreferencedBindingVars = Sets.difference(bindingVars, patternVars);
		if(!unreferencedBindingVars.isEmpty()) {
			logger.warn("View " + view.getName() + ": Unreferenced binding variables: " + unreferencedBindingVars);
		}		
	}

	
	public static void validateView(ViewDefinition view, Logger logger) {
		Set<Var> patternVars = QuadUtils.getVarsMentioned(view.getTemplate());
		
		Set<Var> bindingVars = new HashSet<Var>();
		for(Node bindingVar : view.getMapping().getVarDefinition().getMap().keySet()) {
			bindingVars.add((Var)bindingVar);
		}
		
		Set<Var> unboundPatternVars = Sets.difference(patternVars, bindingVars);
		
		if(!unboundPatternVars.isEmpty()) {
			//throw new RuntimeException("View " + view.getName() + ": Unbound pattern variables: " + unboundPatternVars);
			logger.error("View " + view.getName() + ": Unbound pattern variables: " + unboundPatternVars);
		}

		
		Set<Var> unreferencedBindingVars = Sets.difference(bindingVars, patternVars);
		if(!unreferencedBindingVars.isEmpty()) {
			logger.warn("View " + view.getName() + ": Unreferenced binding variables: " + unreferencedBindingVars);
		}		
	}
}
