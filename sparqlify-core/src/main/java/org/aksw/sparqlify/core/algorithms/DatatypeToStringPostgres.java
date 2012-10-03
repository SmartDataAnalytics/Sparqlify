package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.core.SqlDatatype;

class DatatypeToStringPostgres
	implements DatatypeToString
{
	private Map<String, String> nameToPostgres = new HashMap<String, String>();

	public DatatypeToStringPostgres() {
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
		// bigint nameToPostgres.put("geography", "geography");
	}
	
	/**
	 * Performs a type cast
	 */
	public Factory1<String> asString(SqlDatatype datatype)
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

		final String result = nameToPostgres.get(datatype.getName());
		if(result == null) {
			throw new RuntimeException("No string representation for " + datatype.getName());
		}
		//return result;

		
		return new Factory1<String>() {

			@Override
			public String create(String a) {
				return a + "::" + result;
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
	public Factory1<String> formatString(SqlDatatype datatype)
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
				return a + "::" + result;
			}
		};

		//String result = (String)MultiMethod.invoke(this, "_asString", datatype);
		//return result;		
	}

}