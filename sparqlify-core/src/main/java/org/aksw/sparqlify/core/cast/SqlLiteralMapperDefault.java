package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;



public class SqlLiteralMapperDefault
	implements SqlLiteralMapper
{
	// TODO we need to lookup the 'toString' method for the appropriate type.
	
	private DatatypeToStringPostgres typeSerializer;
	
	//SqlExprSerializerPostgres
	@Deprecated // The typeSerializer should not be needed here anymore
	public SqlLiteralMapperDefault(DatatypeToStringPostgres typeSerializer) {
		this.typeSerializer = typeSerializer;
	}
	
	
	
	@Override
	public String serialize(SqlValue value) {
		
		Object o = value.getValue();
		if(o == null) {
			throw new RuntimeException("Null values should be handled by the serialize system, which can cast them to appropriate types");
		}
		
		String lex = o == null ? "NULL" : "" + o;
		
		//String typeUri = node.getLiteralDatatypeURI();
		//String lex = node.getLiteralLexicalForm();
		TypeToken typeToken = value.getTypeToken();
		
		String result;
		if(typeToken.equals(TypeToken.String)) {
			//result = "'" + lex + "'";
			// TODO We need to cast string literals to an explicit datatype!
			result = "'" + lex + "'::text";
		} else if(typeToken.equals(TypeToken.Date)) {
			result = "'" + lex + "'::timestamp";			
		}
		else {
			result = "" + lex;
		}
		
		return result;
	}
	
	public String serialize(NodeValue value) {
		
		Node node = value.asNode();
		String typeUri = node.getLiteralDatatypeURI();
		String lex = node.getLiteralLexicalForm();
		
		String result;
		if(typeUri == null || typeUri.equals(TypeToken.String.toString())) {
			result = "'" + lex + "'";
		}
		else {
			result = "" + lex;
		}
		
		return result;
		
		/*
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
		}* /

		return result;
		*/
	}
}
