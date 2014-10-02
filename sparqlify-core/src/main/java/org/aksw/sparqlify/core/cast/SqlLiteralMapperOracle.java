package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.TypeSerializer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;



public class SqlLiteralMapperOracle
    implements SqlLiteralMapper
{
    // TODO we need to lookup the 'toString' method for the appropriate type.

    private TypeSerializer typeSerializer;

    //SqlExprSerializerPostgres
    @Deprecated // The typeSerializer should not be needed here anymore
    public SqlLiteralMapperOracle(TypeSerializer typeSerializer) {
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
            result = "TO_CHAR('" + lex + "')";
        } else if(typeToken.equals(TypeToken.Date)) {
            result = "CAST('" + lex + "' AS TIMESTAMP)";
        }
        else {
            result = "" + lex;
        }

        return result;
    }

}
