package org.aksw.sparqlify.core.cast;

import java.util.List;

public class CandidateMethod<T> {
	private MethodEntry<T> method;
	private List<CandidateMethod<T>> coercions;
	
	private MethodDistance distance;
	
	public CandidateMethod(MethodEntry<T> method, List<CandidateMethod<T>> coercions, MethodDistance distance) {
		this.coercions = coercions;
		this.method = method;
		this.distance = distance;
	}

	public MethodEntry<T> getMethod() {
		return method;
	}

	public List<CandidateMethod<T>> getCoercions() {
		return coercions;
	}

	public MethodDistance getDistance() {
		return distance;
	}

	@Override
	public String toString() {
		return "CandidateMethod [method=" + method + ", coercions=" + coercions
				+ ", distance=" + distance + "]";
	}
}