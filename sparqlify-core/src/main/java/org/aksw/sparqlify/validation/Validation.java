package org.aksw.sparqlify.validation;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;

import com.google.common.collect.Sets;


public class Validation {

    public static void validateView(ViewDefinition view, Logger logger) {
        Set<Var> patternVars = QuadPatternUtils.getVarsMentioned(view.getTemplate());

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
