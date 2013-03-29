package org.aksw.sparqlify.views.transform;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.core.RdfViewInstance;
import org.aksw.sparqlify.core.algorithms.OpViewInstanceJoin;
import org.aksw.sparqlify.core.algorithms.ViewInstance;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.IViewDef;
import org.aksw.sparqlify.database.OpExtFilterIndexed;
import org.aksw.sparqlify.sparqlview.OpSparqlViewPattern;

import sparql.EquiMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Var;

public class GetVarsMentioned {
	@SuppressWarnings("unchecked")
	public static Set<Var> getVarsMentioned(Op op) {
		return (Set<Var>)MultiMethod.invokeStatic(GetVarsMentioned.class, "_getVarsMentioned", op);
	}
	
	@Deprecated
	public static Set<Var> _getVarsMentioned(RdfViewInstance op) {
		return new HashSet<Var>(op.getQueryToParentBinding().keySet());
	}

	public static <T extends IViewDef> Set<Var> _getVarsMentioned(OpViewInstanceJoin<T> op) {
		Set<Var> result = new HashSet<Var>();
		for(ViewInstance<T> vi : op.getJoin().getViewInstances()) {
			result.addAll(vi.getBinding().getQueryVars());
		}

		return result;
	}

	public static Set<Var> _getVarsMentioned(OpQuadPattern op) {
		return QuadUtils.getVarsMentioned(op.getPattern());
	}
	
	public static Set<Var> _getVarsMentioned(OpExtend op) {
		Set<Var> result = new HashSet<Var>();
		
		result.addAll(op.getVarExprList().getVars());
		result.addAll(getVarsMentioned(op.getSubOp()));
		
		return result;
	}

	public static Set<Var> _getVarsMentioned(OpExtFilterIndexed op) {
		return getVarsMentioned(op.effectiveOp());
	}

	public static Set<Var> _getVarsMentioned(OpSparqlViewPattern op) {
		return getVarsMentioned(op.effectiveOp());
	}
	
	/*
	public static Set<Var> getVarsMentioned(OpUnion op) {
		Set<Var> tmp = getVarsMentioned(op.getLeft());
		tmp.addAll(getVarsMentioned(op.getRight()));
		
		return tmp;
	}*/
	
	public static Set<Var> _getVarsMentioned(OpRdfViewPattern op) {
		Set<Var> result = new HashSet<Var>();
		
		for(RdfViewInstance item : op.getConjunction().getViewBindings()) {
			EquiMap<Var, Node> equiMap = item.getBinding().getEquiMap();

			result.addAll(equiMap.getEquivalences().asMap().keySet());
			result.addAll(equiMap.getKeyToValue().keySet());
			
			//result.addAll(item.getQueryToParentBinding().keySet());
		}
		
		return result;
	}

	public static Set<Var> _getVarsMentioned(Op1 op) {
		return getVarsMentioned(op.getSubOp());
	}

	public static Set<Var> _getVarsMentioned(Op2 op) {
		Set<Var> tmp = getVarsMentioned(op.getLeft());
		tmp.addAll(getVarsMentioned(op.getRight()));
		
		return tmp;
	}

	public static Set<Var> _getVarsMentioned(OpN op) {
		Set<Var> result = new HashSet<Var>();
		for(Op item : op.getElements()) {
			result.addAll(getVarsMentioned(item));
		}
		
		return result;
	}

}