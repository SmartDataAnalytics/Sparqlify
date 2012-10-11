package org.aksw.sparqlify.core.datatypes;

import java.lang.reflect.Method;

public class InvocableMethod
	implements Invocable
{
	private Object object;
	private Method method;

	public InvocableMethod(Method method) {
		this(null, method);
	}

	public InvocableMethod(Object object, Method method) {
		this.method = method;
		this.object = object;

		if(!method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	@Override
	public Object invoke(Object... args) {
		Object result;
		try {
			result = method.invoke(object, args);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public Object getObject() {
		return object;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return "InvocableMethod [object=" + object + ", method=" + method + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		InvocableMethod other = (InvocableMethod) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}
}

