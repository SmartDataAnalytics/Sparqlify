package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

public class MethodEntry<T, I> {
	private String name;
	private MethodSignature<T> signature;
	private I data;
	
	public MethodEntry(String name, MethodSignature<T> signature, I data) {
		super();
		this.signature = signature;
		this.data = data;
	}
	
	public String getName() {
		return name;
	}

	public MethodSignature<T> getSignature() {
		return signature;
	}

	public void setSignature(MethodSignature<T> signature) {
		this.signature = signature;
	}

	public I getData() {
		return data;
	}

	public void setData(I data) {
		this.data = data;
	}
}