package org.aksw.sparqlify.core.interfaces;

import java.util.Map;

import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;

public interface IViewDef {
	String getName();
	QuadPattern getTemplate();
	
	RestrictionManagerImpl getVarRestrictions();
	
	VarDefinition getVarDefinition();
	IViewDef copyRenameVars(Map<Var, Var> oldToNew);

	
}
