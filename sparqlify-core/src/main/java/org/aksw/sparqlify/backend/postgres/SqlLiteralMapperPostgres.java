package org.aksw.sparqlify.backend.postgres;

import java.util.Date;
import java.util.function.UnaryOperator;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.cast.SqlLiteralMapper;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_ParseDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SqlLiteralMapperPostgres
    implements SqlLiteralMapper
{
	
	private static final Logger logger = LoggerFactory.getLogger(SqlLiteralMapperPostgres.class);

    // TODO we need to lookup the 'toString' method for the appropriate type.

    protected SqlEscaper sqlEscaper;
    protected DatatypeToString typeSerializer;

    //SqlExprSerializerPostgres
    public SqlLiteralMapperPostgres(DatatypeToString typeSerializer, SqlEscaper sqlEscaper) {
        this.typeSerializer = typeSerializer;
        this.sqlEscaper = sqlEscaper;
    }



    @Override
    public String serialize(SqlValue value) {

        Object o = value.getValue();
        if(o == null) {
            throw new RuntimeException("Null values should be handled by the serialize system, which can cast them to appropriate types");
        }

        TypeToken typeToken = value.getTypeToken();
        
        //String lex = o == null ? "NULL" : "" + o;

        
        String result;
        boolean applyCast;
        if(o instanceof Number) {
            result = "" + o;
            applyCast = false;
        } else if(o instanceof Date) {
            Date date = (Date)o;
            logger.debug("HACK used - clean up when revising type system");
            result = SqlExprEvaluator_ParseDate.DATE.getDateFormat().format(date);
            applyCast = true;
        } else {
            String lex = "" + o;
            result = sqlEscaper.escapeStringLiteral(lex);
            applyCast = true;
        }

        if(applyCast) {
            UnaryOperator<String> castApplier = typeSerializer.asString(typeToken);
            result = castApplier.apply(result);
        }
        

        //String typeUri = node.getLiteralDatatypeURI();
        //String lex = node.getLiteralLexicalForm();

        
        //UnaryOperator<String> valueSerializer = typeSerializer.asString(typeToken);
        
        
//        
//        
//        String result;
//        if(typeToken.equals(TypeToken.String)) {
//            //result = "'" + lex + "'";
//            // TODO We need to cast string literals to an explicit datatype!
//            result = sqlEscaper.escapeStringLiteral(lex); + "'::text";
//        } else if(typeToken.equals(TypeToken.Date)) {
//            Date date = (Date)o;
//            lex = SqlExprEvaluator_ParseDate.defaultDateFormat.format(date);
//            result = "'" + lex + "'::date";
//        } else if(typeToken.equals(TypeToken.DateTime)) {
//            result = "'" + lex + "'::timestamp";
//        }
//        else {
//            result = "" + lex;
//        }

        return result;
    }
//
//    public String serialize(NodeValue value) {
//
//        Node node = value.asNode();
//        String typeUri = node.getLiteralDatatypeURI();
//        String lex = node.getLiteralLexicalForm();
//
//        String result;
//        if(typeUri == null || typeUri.equals(TypeToken.String.toString())) {
//            result = "'" + lex + "'";
//        }
//        else {
//            result = "" + lex;
//        }
//
//        return result;
//
//        /*
//        if(SparqlifyConstants.nvTypeError.equals(value)) {
//            value = NodeValue.FALSE;
//        }
//
//        Object o = NodeValueUtilsSparqlify.getValue(value);
//
//        String typeName = value.asNode().getLiteralDatatype().toString();
//        TypeToken typeToken = TypeToken.alloc(typeName);
//        String result = SqlExprSerializerPostgres.serializeConstantPostgres(typeSerializer, o, typeToken);
//
//        /*
//        String result;
//        if(o instanceof Number) {
//            result = "" + o;
//        } else {
//            result = "\"" + o + "\"";
//        }* /
//
//        return result;
//        */
//    }
}
