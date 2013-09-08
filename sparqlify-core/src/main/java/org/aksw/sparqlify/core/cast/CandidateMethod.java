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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coercions == null) ? 0 : coercions.hashCode());
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CandidateMethod other = (CandidateMethod) obj;
		if (coercions == null) {
			if (other.coercions != null)
				return false;
		} else if (!coercions.equals(other.coercions))
			return false;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}
}