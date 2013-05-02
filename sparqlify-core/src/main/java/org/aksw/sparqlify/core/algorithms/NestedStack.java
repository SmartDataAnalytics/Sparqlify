package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NestedStack<T>
{
	private NestedStack<T> parent;	
	private T value;

	public NestedStack(NestedStack<T> parent, T value) {
		super();
		this.parent = parent;
		this.value = value;
	}
	
	public NestedStack<T> getParent() {
		return parent;
	}

	public T getValue() {
		return value;
	}
	
	
	public List<T> asList() {
		List<T> result = new ArrayList<T>();
		
		NestedStack<T> current = this;
		while(current != null) {
			result.add(current.getValue());
			current = current.parent;
		}
		
		Collections.reverse(result);
		
		return result;
	}
}