package org.aksw.sparqlify.core.datatypes;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.core.TypeToken;


/**
 * Not used
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class XMethodDecl {

	private String name;
	private MethodSignature<TypeToken> typeSignature;
	private Invocable invocable;
	
	public XMethodDecl(String name, MethodSignature<TypeToken> typeSignature, Invocable invocable) {
		this.name = name;
		this.typeSignature = typeSignature;
		this.invocable = invocable;
	}
	
	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public MethodSignature<TypeToken> getTypeSignature() {
		return typeSignature;
	}



	public void setTypeSignature(MethodSignature<TypeToken> typeSignature) {
		this.typeSignature = typeSignature;
	}



	public Invocable getInvocable() {
		return invocable;
	}



	public void setInvocable(Invocable invocable) {
		this.invocable = invocable;
	}



}
