package org.aksw.sparqlify.core.cast;

import java.util.List;

public class CandidateMethod<T, I> {
	private MethodEntry<T, I> method;
	private List<MethodEntry<T, I>> coercions;
	
	private MethodDistance distance;
	
	public CandidateMethod(MethodEntry<T, I> method, List<MethodEntry<T, I>> coercions, MethodDistance distance) {
		this.coercions = coercions;
		this.method = method;
		this.distance = distance;
	}

	public MethodEntry<T, I> getMethod() {
		return method;
	}

	public List<MethodEntry<T, I>> getCoercions() {
		return coercions;
	}

	public MethodDistance getDistance() {
		return distance;
	}
}