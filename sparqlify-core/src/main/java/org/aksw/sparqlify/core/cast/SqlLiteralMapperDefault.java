package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.algorithms.SqlExprSerializerPostgres;
import org.aksw.sparqlify.expr.util.NodeValueUtils;

import com.hp.hpl.jena.sparql.expr.NodeValue;



public class SqlLiteralMapperDefault
	implements SqlLiteralMapper
{
	// todo: we need to lookup the 'toString' method for the appropriate type.
	
	private DatatypeToStringPostgres typeSerializer;
	
	//SqlExprSerializerPostgres
	public SqlLiteralMapperDefault(DatatypeToStringPostgres typeSerializer) {
		this.typeSerializer = typeSerializer;
	}
	
	
	@Override
	public String serialize(NodeValue value) {
		if(SparqlifyConstants.nvTypeError.equals(value)) {
			value = NodeValue.FALSE;
		}
		
		Object o = NodeValueUtils.getValue(value);

		String typeName = value.asNode().getLiteralDatatype().toString();
		TypeToken typeToken = TypeToken.alloc(typeName);
		String result = SqlExprSerializerPostgres.serializeConstantPostgres(typeSerializer, o, typeToken);
		
		/*
		String result;
		if(o instanceof Number) {
			result = "" + o;
		} else {
			result = "\"" + o + "\"";
		}*/

		return result;
	}
}
