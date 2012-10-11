package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.openjena.atlas.io.IndentedWriter;

public class S_Constant
	extends SqlExprConstantBase
{
	private Object value;
	
	public S_Constant(Object value, TypeToken datatype) {
		super(datatype);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T)value;
	}
	

	@Override
	public String toString() {
		return "S_Constant [value=" + value + ", getDatatype()="
				+ getDatatype() + "]";
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print("" + value + " (" + getDatatype() + ")");
		//writer.println("Concat");
		//writeArgs(writer);
	}

	
	public static S_Constant create(Object value, DatatypeSystem datatypeSystem) {
		//Object value = ExprUtils.getJavaObject(nv);
		//SqlDatatype datatype = datatypeSystem.getByClass(value.getClass());

		TypeToken datatype = datatypeSystem.getTokenForClass(value.getClass());
		S_Constant result = new S_Constant(value, datatype);
		
		return result;
	}
	

	


	// FIXME Following methods do not check the datatype
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		S_Constant other = (S_Constant) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
