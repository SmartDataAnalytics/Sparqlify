package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.graph.Node;

public class XClassImpl
	implements XClass
{
	//private String name;
	private DatatypeSystem datatypeSystem;
	private TypeToken typeToken;
	
	
	private Node xsd;
	
	public Node getXsd() {
		return xsd;
	}
	
	// The java class matching the datatype best
	// used for conversions
	// TODO This attribute is currently not considered in equals/hashCode
	private Class<?> correspondingClass;

	public XClassImpl(DatatypeSystem datatypeSytem, TypeToken typeToken, Node xsd, Class<?> correspondingClass)
	{
		this.datatypeSystem = datatypeSytem;
		this.typeToken = typeToken;
		this.correspondingClass = correspondingClass;
		this.xsd = xsd;
	}

	/*
	public XClassImpl(String name, Class<?> correspondingClass)
	{
		this.name = name;
		this.correspondingClass = correspondingClass;
	}
	
	public XClassImpl(String name, Node xsd, Class<?> correspondingClass)
	{
		this.name = name;
		this.xsd = xsd;
		this.correspondingClass = correspondingClass;
	}
	*/
	
	public List<XClass> getDirectSuperClasses() {
		List<TypeToken> types = datatypeSystem.getDirectSuperClasses(typeToken);
		
		List<XClass> result = XClassUtils.resolve(datatypeSystem, types);
		return result;
	}
	
	public DatatypeSystem getDatatypeSystem() {
		return datatypeSystem;
	}
	
	public String getName() {
		return typeToken.toString();
	}

	public TypeToken getToken() {
		return typeToken;
	}
	
	
	@Override
	public String toString() {
		return typeToken.toString();
	}

	@Override
	public Class<?> getCorrespondingClass() {
		return correspondingClass;
	}

	
	/**
	 * Checks this class is a super (or equal) class of that.
	 * 
	 */
	@Override
	public boolean isAssignableFrom(XClass that) {
		
		if(!(that instanceof XClassImpl)) {
			return false;
		}
		
		XClassImpl t = (XClassImpl)that; 
		
		boolean result = datatypeSystem.isSuperClassOf(this.typeToken, t.typeToken);
		
		return result;
	}
	
	
}
