package org.aksw.sparqlify.core.datatypes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

public class XMethodImpl implements XMethod
{
	private String name;
	private MethodSignature<XClass> signature;
	private Invocable invocable;

	
	
	public XMethodImpl(String name, MethodSignature<XClass> signature,
			Invocable invocable) {
		super();
		this.name = name;
		this.signature = signature;
		this.invocable = invocable;
	}



	public String getName() {
		return name;
	}



	public MethodSignature<XClass> getSignature() {
		return signature;
	}



	public Invocable getInvocable() {
		return invocable;
	}



	@Override
	public String toString() {
		return "XMethodImpl [name=" + name + ", signature=" + signature
				+ ", invocable=" + invocable + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((invocable == null) ? 0 : invocable.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
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
		XMethodImpl other = (XMethodImpl) obj;
		if (invocable == null) {
			if (other.invocable != null)
				return false;
		} else if (!invocable.equals(other.invocable))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	
	public static List<XClass> resolveList(DatatypeSystem datatypeSystem, Class<?>[] classes) {
		List<XClass> result = new ArrayList<XClass>(classes.length);
		
		for(Class<?> clazz : classes) {
			XClass tmp = resolveClass(clazz, datatypeSystem);
			result.add(tmp);
		}
		
		
		return result;
	}
	
	public static XClass resolveClass(Class<?> clazz, DatatypeSystem datatypeSystem) {
		XClass result = datatypeSystem.getByClass(clazz);
		if(result == null) {
			throw new RuntimeException("No appropriate XClass for " + clazz);
		}
		
		return result;
	}

	
	
	public static XMethod createFromMethod(String name, DatatypeSystem datatypeSystem, Object object, Method method) {
		
		XClass returnType = resolveClass(method.getReturnType(), datatypeSystem);
		List<XClass> argTypes = resolveList(datatypeSystem, method.getParameterTypes());
		
		MethodSignature<XClass> resolvedSignature = new MethodSignature<XClass>(returnType, false, argTypes);
		
		
		InvocableMethod invocable = new InvocableMethod(object, method);
	
		//XMethod result = new XMethodImpl(method.getName(), resolvedSignature, invocable);
		XMethod result = new XMethodImpl(name, resolvedSignature, invocable);
		
		
		return result;
	}

}

