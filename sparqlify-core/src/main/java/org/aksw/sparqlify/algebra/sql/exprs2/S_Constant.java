package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.apache.jena.atlas.io.IndentedWriter;

import org.apache.jena.datatypes.TypeMapper;



public class S_Constant
	extends SqlExprConstantBase
{
	public static final S_Constant TRUE = new S_Constant(SqlValue.TRUE);
	public static final S_Constant FALSE = new S_Constant(SqlValue.FALSE);
	
	public static final TypeMapper typeMapper = TypeMapper.getInstance();

// TODO I am still not sure whether type-errors should be modeled as datatypes
// or as exceptions being thrown during evaluation.
// The advantage of using datatype is, that e.g. logical operators can consider
// them during evaluation.
	public static final S_Constant TYPE_ERROR = new S_Constant(SqlValue.TYPE_ERROR); //SparqlifyConstants.nvTypeError);
	
	//private NodeValue value;
	private SqlValue value;

	/**
	 * TODO The question is whether S_Constant arguments should be NodeValues or Object-datatype pairs
	 * Variables are eventually just type tokens (e.g. int or maybe sql:int) wheras constants are xsd:int.
	 * Actually, we just have to register sql:int for jena to recognize it as an integer.
	 * 
	 * 
	 * @param value
	 */	
	public S_Constant(SqlValue value) {
		super(value.getTypeToken());
		this.value = value;
	}

	public static S_Constant create(SqlValue value) {
		S_Constant result = new S_Constant(value);
		return result;
	}
	
//	public S_Constant(TypeToken typeToken, NodeValue value) {
//		super(typeToken);
//		this.value = value;
//	}
	
	
//	public static S_Constant create(NodeValue value) {
//		Node node = value.asNode();
//		if(!node.isLiteral()) {
//			throw new RuntimeException("Literal expected; got: " + value);
//		}
//		
//		String typeUri = node.getLiteralDatatypeURI();
//		TypeToken typeToken;
//		if(typeUri == null) {
//			typeToken = TypeToken.String;
//		}
//		else {
//			typeToken = TypeToken.alloc(typeUri); 
//		}
//		
//		S_Constant result = new S_Constant(typeToken, value);
//		return result;
//	}
	
	// For null values
	public S_Constant(TypeToken typeName) {
		super(typeName);
		this.value = new SqlValue(typeName, null);
	}
	
	/**
	 * For use in logical operators: value becomes a discriminator value
	 * 
	 * @param typeToken
	 * @param value
	 */
	@Deprecated
	public S_Constant(TypeToken typeName, String value) {
		super(typeName);
		this.value = new SqlValue(typeName, value);
//		this.value = NodeValue.makeString(value);
		//throw new RuntimeException("Deprecated");
	}
	
	
	public SqlValue getValue()
	{
		return value;
	}
	
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("" + value);
	}
	
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



	/*
	@Override
	public <T> T getValue() {
		Object tmp = NodeValueUtils.getValue(nodeValue);
		T tmp = (T)
	}
	*/
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

}



/**
 * Experimental class:
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
//class S_ConstantOld
//	extends SqlExprConstantBase
//{
//	public static final S_Constant TRUE = new S_Constant(NodeValue.TRUE);
//	public static final S_Constant FALSE = new S_Constant(NodeValue.FALSE);
//	
//	public static final TypeMapper typeMapper = TypeMapper.getInstance();
//
//	// TODO I am still not sure whether type-errors should be modeled as datatypes
//	// or as exceptions being thrown during evaluation.
//	// The advantage of using datatype is, that e.g. logical operators can consider
//	// them during evaluation.
//	public static final S_Constant TYPE_ERROR = new S_Constant(SparqlifyConstants.nvTypeError);
//	
//	private NodeValue value;
//	
//	/**
//	 * TODO The question is whether S_Constant arguments should be NodeValues or Object-datatype pairs
//	 * Variables are eventually just type tokens (e.g. int or maybe sql:int) wheras constants are xsd:int.
//	 * Actually, we just have to register sql:int for jena to recognize it as an integer.
//	 * 
//	 * 
//	 * @param value
//	 */	
//	public S_Constant(NodeValue value) {
//		super(TypeToken.alloc(value.asNode().getLiteralDatatypeURI()));
//		this.value = value;
//	}
//	
//	
//	public S_Constant(TypeToken typeToken, NodeValue value) {
//		super(typeToken);
//		this.value = value;
//	}
//	
//
//	public static S_Constant create(NodeValue value) {
//		Node node = value.asNode();
//		if(!node.isLiteral()) {
//			throw new RuntimeException("Literal expected; got: " + value);
//		}
//		
//		String typeUri = node.getLiteralDatatypeURI();
//		TypeToken typeToken;
//		if(typeUri == null) {
//			typeToken = TypeToken.String;
//		}
//		else {
//			typeToken = TypeToken.alloc(typeUri); 
//		}
//		
//		S_Constant result = new S_Constant(typeToken, value);
//		return result;
//	}
//	
//	// For null values
//	public S_Constant(TypeToken typeName) {
//		super(typeName);
//	}
//
//	/**
//	 * For use in logical operators: value becomes a discriminator value
//	 * 
//	 * @param typeToken
//	 * @param value
//	 */
//	@Deprecated
//	public S_Constant(TypeToken typeName, String value) {
//		super(typeName);
//		this.value = NodeValue.makeString(value);
//	}
//	
//	
//	public NodeValue getValue()
//	{
//		return value;
//	}
//	
//	
//	@Override
//	public void asString(IndentedWriter writer) {
//		writer.print(value);
//	}
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
//	
//	/*
//	@Override
//	public <T> T getValue() {
//		Object tmp = NodeValueUtils.getValue(nodeValue);
//		T tmp = (T)
//	}
//	*/
//}
