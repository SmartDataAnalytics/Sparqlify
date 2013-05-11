package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.interfaces.IViewDef;

public class RecursionResult<T extends IViewDef, C> {
	private ViewInstanceJoin<T> viewInstances;
	private C finalContext;
	
	public RecursionResult(ViewInstanceJoin<T> viewInstances, C finalContext) {
		super();
		this.viewInstances = viewInstances;
		this.finalContext = finalContext;
	}
	public ViewInstanceJoin<T> getViewInstances() {
		return viewInstances;
	}
	public C getFinalContext() {
		return finalContext;
	}
	
	public static <T extends IViewDef, C> RecursionResult<T, C> create(ViewInstanceJoin<T> viewInstanceJoin, C finalContext) {
		RecursionResult<T, C> result = new RecursionResult<T, C>(viewInstanceJoin, finalContext);
		return result;
	}
}