package org.aksw.sparqlify.core.datatypes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerDefault;
import org.aksw.sparqlify.type_system.MethodSignature;

public class XMethodImpl implements XMethod
{
	private String name;
	private MethodSignature<XClass> signature;
	private Invocable invocable;
	private SqlFunctionSerializer serializer;
	
	
	public XMethodImpl(String name, MethodSignature<XClass> signature,
			Invocable invocable) {
		this(name, signature, invocable, new SqlFunctionSerializerDefault(name));
	}

	public XMethodImpl(String name, MethodSignature<XClass> signature,
			Invocable invocable, SqlFunctionSerializer serializer) {
		super();
		this.name = name;
		this.signature = signature;
		this.invocable = invocable;
		this.serializer = serializer;
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

	
	public SqlFunctionSerializer getSerializer() {
		return serializer;
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

	
	public static List<XClass> resolveList(TypeSystem datatypeSystem, Class<?>[] classes) {
		List<XClass> result = new ArrayList<XClass>(classes.length);
		
		for(Class<?> clazz : classes) {
			XClass tmp = resolveClass(clazz, datatypeSystem);
			result.add(tmp);
		}
		
		
		return result;
	}
	
	public static XClass resolveClass(Class<?> clazz, TypeSystem datatypeSystem) {
		XClass result = datatypeSystem.getByClass(clazz);
		if(result == null) {
			throw new RuntimeException("No appropriate XClass for " + clazz);
		}
		
		return result;
	}


	public static XClass resolveClass(TypeToken typeName, TypeSystem datatypeSystem) {
		XClass result = datatypeSystem.getByName(typeName);
		if(result == null) {
			throw new RuntimeException("No appropriate XClass for " + typeName);
		}
		
		return result;
	}

	public static List<XClass> resolveList(TypeSystem datatypeSystem, List<TypeToken> typeNames) {
		List<XClass> result = new ArrayList<XClass>(typeNames.size());
		
		for(TypeToken typeName : typeNames) {
			XClass tmp = resolveClass(typeName, datatypeSystem);
			result.add(tmp);
		}
		
		
		return result;
	}

	
	public static XMethod create(TypeSystem datatypeSystem, String name, MethodSignature<TypeToken> signature) {
		XClass returnType = resolveClass(signature.getReturnType(), datatypeSystem);
		List<XClass> argTypes = resolveList(datatypeSystem, signature.getParameterTypes());
		
		MethodSignature<XClass> resolvedSignature = new MethodSignature<XClass>(returnType, argTypes, null);
		
		
		//InvocableMethod invocable = new InvocableMethod(object, method);
		InvocableMethod invocable = null;
	
		//XMethod result = new XMethodImpl(method.getName(), resolvedSignature, invocable);
		XMethod result = new XMethodImpl(name, resolvedSignature, invocable);
		
		
		return result;

	}


	
	public static XMethod createFromMethod(String name, TypeSystem datatypeSystem, Object object, Method method) {
		
		XClass returnType = resolveClass(method.getReturnType(), datatypeSystem);
		List<XClass> argTypes = resolveList(datatypeSystem, method.getParameterTypes());
		
		MethodSignature<XClass> resolvedSignature = new MethodSignature<XClass>(returnType, argTypes, null);
		
		
		InvocableMethod invocable = new InvocableMethod(object, method);
	
		//XMethod result = new XMethodImpl(method.getName(), resolvedSignature, invocable);
		XMethod result = new XMethodImpl(name, resolvedSignature, invocable);
		
		
		return result;
	}

}

