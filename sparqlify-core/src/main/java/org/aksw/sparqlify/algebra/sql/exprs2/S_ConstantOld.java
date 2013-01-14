package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.openjena.atlas.io.IndentedWriter;


//public class S_Constant
//	extends SqlExprConstantBase
//{
//	private Object value;
//	
//	public static final S_Constant TRUE = new S_Constant(TypeToken.Boolean, true);
//	public static final S_Constant FALSE = new S_Constant(TypeToken.Boolean, false);
//	public static final S_Constant TYPE_ERROR = new S_Constant(TypeToken.TypeError, null);
//	
//	public S_Constant(TypeToken datatype, Object value) {
//		super(datatype);
//		this.value = value;
//	}
//
//	@SuppressWarnings("unchecked")
//	public <T> T getValue() {
//		return (T)value;
//	}
//	
//
//	@Override
//	public String toString() {
//		return "S_Constant [value=" + value + ", getDatatype()="
//				+ getDatatype() + "]";
//	}
//
//	@Override
//	public void asString(IndentedWriter writer) {
//		writer.print("" + value + " (" + getDatatype() + ")");
//		//writer.println("Concat");
//		//writeArgs(writer);
//	}
//
//	
//	public static S_Constant create(Object value, DatatypeSystem datatypeSystem) {
//		//Object value = ExprUtils.getJavaObject(nv);
//		//SqlDatatype datatype = datatypeSystem.getByClass(value.getClass());
//
//		TypeToken typeName = datatypeSystem.getTokenForClass(value.getClass());
//		//XClass datatype = datatypeSystem.getByName(typeName);
//		S_Constant result = new S_Constant(typeName, value);
//		
//		return result;
//	}
//	
//
//	
//
//
//	// FIXME Following methods do not check the datatype
//	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + ((value == null) ? 0 : value.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		S_Constant other = (S_Constant) obj;
//		if (value == null) {
//			if (other.value != null)
//				return false;
//		} else if (!value.equals(other.value))
//			return false;
//		return true;
//	}
//	
//	
//}
