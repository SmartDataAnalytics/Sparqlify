package org.aksw.sparqlify.core.algorithms;

import java.util.Calendar;
import java.util.function.UnaryOperator;

import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.sparql.expr.NodeValue;
import org.postgis.PGgeometry;


public class SqlExprSerializerPostgres
    extends SqlExprSerializerDefault
{
    public SqlExprSerializerPostgres() {
        super(new DatatypeToStringPostgres());
    }


    public String serializeConstant(Object value, TypeToken datatype) {
        String result = serializeConstantPostgres(datatypeSerializer, value, datatype);
        return result;
    }


    public static String serializeConstantPostgres(DatatypeToString datatypeSerializer, Object value, TypeToken datatype) {


        if(datatype.equals(TypeToken.TypeError)) {
            return "FALSE";
        }


        if(value == null) {
            //String cast = "::" + datatypeSerializer.asString(datatype);

            UnaryOperator<String> caster = datatypeSerializer.asString(datatype);

            return caster.apply("NULL");
        } else if(value instanceof NodeValue) {
            if(true) {
                throw new RuntimeException("HACK");
            }
            return ((NodeValue) value).asQuotedString();
        }
        else if(value instanceof String) {
            return quoteStr(value.toString());
        } else if(value instanceof Number) {
            return value.toString();
        } else if(value instanceof Calendar) {
            java.sql.Timestamp sqlDateTime = new java.sql.Timestamp(((Calendar)value).getTime().getTime());
            return quoteStr(sqlDateTime.toString());
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof PGgeometry) {
            //return "'SRID=4326;" + value.toString() + "'::geometry";
            return "'SRID=4326;" + value.toString() + "'";
        } else {
            throw new RuntimeException("Don't know how to serialize " + value + " to an SQL string");
        }
    }


    // Copy from SDB's SQLUtils

    static private final String strQuoteChar = "'" ;
    static private final String strQuoteCharEsc = strQuoteChar+strQuoteChar ;

    static private final String strQuoteChar2 = "\\" ;
    static private final String strQuoteCharEsc2 = strQuoteChar2+strQuoteChar2 ;


    static private final String[] strChar =       { strQuoteChar, strQuoteChar2 } ;
    static private final String[] strCharEsc =    { strQuoteCharEsc, strQuoteCharEsc2 } ;

    public static String escapeStr(String s)
    { return replace(s, strChar, strCharEsc) ; }


    public static String quoteStr(String s)
    {
        s = escapeStr(s) ;
        return strQuoteChar+s+strQuoteChar ;
    }

    public static String replace(String str, String[] fromArray, String[] toArray)
    {
        for ( int i = 0 ; i < fromArray.length ; i++ )
            str = str.replace(fromArray[i], toArray[i]) ;
        return str ;
    }

}

/*
class DatatypeToStringMySql
    implements DatatypeToString
{
    public String asString(TokenType datatype)
    {
        String result = (String)MultiMethod.invoke(this, "_asString", datatype);
        return result;
    }

    public String _asString(TokenTypeDateTime datatype) {
        return "datetime";
    }

    public String _asString(TokenTypeReal datatype) {
        return "decimal";
    }

    public String _asString(TokenTypeInteger datatype) {
        return "decimal";
    }

    public String _asString(TokenTypeString datatype) {
        return "char";
    }

    public String _asString(TokenTypeBigInteger datatype) {
        return "decimal";
    }

}*/






/*
class ExprSerializerMySql
    extends ExprSerializerDefault
{

    public ExprSerializerMySql() {
        super(new DatatypeToStringMySql());
    }

    public String serializeConstant(Object value, TokenType datatype) {

        if(value == null) {
            return "CAST(NULL AS " + datatypeSerializer.asString(datatype) + ")";
        } else if(value instanceof String) {
            return SQLUtils.quoteStr(value.toString());
        } else {
            return value.toString();
        }
    }

}
*/