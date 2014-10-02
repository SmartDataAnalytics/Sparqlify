package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.util.factory.Factory1;
import org.aksw.sparqlify.core.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeSerializerOracle
    implements TypeSerializer
{

    private static final Logger logger = LoggerFactory.getLogger(TypeSerializerOracle.class);

    private Map<String, String> nameToPostgres = new HashMap<String, String>();

    public TypeSerializerOracle() {
        // TODO: Use the datatype system map for reverse mapping
        nameToPostgres.put("boolean", "boolean");
        nameToPostgres.put("float", "double precision");
        nameToPostgres.put("double", "double precision");
        nameToPostgres.put("integer", "integer");
        nameToPostgres.put("string", "text");
        nameToPostgres.put("geometry", "geometry");
        nameToPostgres.put("geography", "geography");
        nameToPostgres.put("int", "integer");
        nameToPostgres.put("long", "bigint");

        //nameToPostgres.put("d", "datetime");
        nameToPostgres.put("datetime", "date");
        nameToPostgres.put("dateTime", "date");
        // bigint nameToPostgres.put("geography", "geography");

        nameToPostgres.put("timestamp", "timestamp");

        // FIXME Not sure if we really have to may every type explicitely here
        // I guess this should be inferred from the config
        nameToPostgres.put("int4", "int4");
        nameToPostgres.put("text", "text");
        nameToPostgres.put("VARCHAR", "VARCHAR");
        nameToPostgres.put("DOUBLE", "DOUBLE");
        nameToPostgres.put("INTEGER", "INTEGER");
        nameToPostgres.put("BIGINT", "BIGINT");
        nameToPostgres.put("REAL", "REAL");
        nameToPostgres.put("TIMESTAMP", "TIMESTAMP");
        nameToPostgres.put("DATE", "DATE");
        nameToPostgres.put("BOOLEAN", "BOOLEAN");
        nameToPostgres.put("VARBINARY", "VARBINARY");
        nameToPostgres.put("CHAR", "CHAR");


        //nameToPostgres.put("bpchar", "BPCHAR");
    }

    /**
     * Performs a type cast
     */
    public Factory1<String> asString(TypeToken datatype)
    {
        /*
        if(datatype.getName().equals("geography")) {
            return new Factory1<String>() {

                @Override
                public String create(String a) {
                    return "ST_AsText(" + a +")";
                }

            };
        }*/

        String tmp = nameToPostgres.get(datatype.getName());
        if(tmp == null) {
            tmp = datatype.getName();
            //System.err.println("WARNING: Datatype not checked for db support");
            logger.trace("WARNING: Datatype not checked for db support");
            //throw new RuntimeException("No string representation for " + datatype.getName());
        }

        final String result = tmp;
        //return result;


        return new Factory1<String>() {

            @Override
            public String create(String a) {
                return "CAST(" + a + " AS " + result + ")";
            }
        };

        //String result = (String)MultiMethod.invoke(this, "_asString", datatype);
        //return result;
    }


    /**
     * Uses custom function to convert between types
     * (e.g ST_AsText(geometry) for conversion to string
     *
     * @param datatype
     * @return
     */
    public Factory1<String> formatString(TypeToken datatype)
    {
        if(datatype.getName().equals("geometry") || datatype.getName().equals("geography")) {
            return new Factory1<String>() {

                @Override
                public String create(String a) {
                    return "ST_AsText(" + a +")";
                }

            };
        }

        final String result = nameToPostgres.get(datatype.getName());
        if(result == null) {
            throw new RuntimeException("No string representation for " + datatype.getName());
        }
        //return result;


        return new Factory1<String>() {

            @Override
            public String create(String a) {
                return "CAST(" + a + " AS " + result + ")";
            }
        };

        //String result = (String)MultiMethod.invoke(this, "_asString", datatype);
        //return result;
    }

}