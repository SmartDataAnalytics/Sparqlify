package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Experimental class:
 * Let's assume that during translation 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class S_Constant
	extends SqlExprConstantBase
{
	public static final S_Constant TRUE = new S_Constant(NodeValue.TRUE);
	public static final S_Constant FALSE = new S_Constant(NodeValue.FALSE);
	
	public static final TypeMapper typeMapper = TypeMapper.getInstance();

	// TODO I am still not sure whether type-errors should be modeled as datatypes
	// or as exceptions being thrown during evaluation.
	// The advantage of using datatype is, that e.g. logical operators can consider
	// them during evaluation.
	public static final S_Constant TYPE_ERROR = new S_Constant(SparqlifyConstants.nvTypeError);
	
	private NodeValue value;
	
	public S_Constant(NodeValue value) {
		super(TypeToken.alloc(value.asNode().getLiteralDatatypeURI()));
		this.value = value;
	}
	
	// For null values
	public S_Constant(TypeToken typeName) {
		super(typeName);
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
		this.value = NodeValue.makeString(value);
	}
	
	
	public NodeValue getValue()
	{
		return value;
	}
	
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(value);
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
}
