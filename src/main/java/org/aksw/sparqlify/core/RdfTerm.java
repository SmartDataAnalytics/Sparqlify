package org.aksw.sparqlify.core;

public class RdfTerm<T> {
	protected T type;
	protected T value;
	protected T language;
	protected T datatype;
		
	public RdfTerm(T type, T value, T language, T datatype) {
		super();
		this.type = type;
		this.value = value;
		this.language = language;
		this.datatype = datatype;
	}

	public RdfTerm(RdfTerm<? extends T> other) {
		this(other.getType(), other.getValue(), other.getLanguage(), other.getDatatype());
	}

	public RdfTerm(T value) {
		this.value = value;
	}

	public RdfTerm() {
	}

	public T getType() {
		return type;
	}

	public void setType(T type) {
		this.type = type;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getLanguage() {
		return language;
	}

	public void setLanguage(T language) {
		this.language = language;
	}

	public T getDatatype() {
		return datatype;
	}

	public void setDatatype(T datatype) {
		this.datatype = datatype;
	}
}
