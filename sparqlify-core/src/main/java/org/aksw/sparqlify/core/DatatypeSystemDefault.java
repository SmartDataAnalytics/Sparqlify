package org.aksw.sparqlify.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.Caster;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;




/**
 * An alternative implementation of SqlDatatype
 * that does not use "one class per datatype approach"
 * 
 * Trying out whether this is better in practice.
 * 
 * @author raven
 *
 */
class SqlDatatypeImpl
	implements SqlDatatype
{
	private String name;
	
	private Node xsd;
	
	public Node getXsd() {
		return xsd;
	}
	
	// The java class matching the datatype best
	// used for conversions
	// TODO This attribute is currently not considered in equals/hashCode
	private Class<?> correspondingClass;

	public SqlDatatypeImpl(String name)
	{
		this.name = name;
		this.correspondingClass = null;
	}

	public SqlDatatypeImpl(String name, Class<?> correspondingClass)
	{
		this.name = name;
		this.correspondingClass = correspondingClass;
	}
	
	public SqlDatatypeImpl(String name, Node xsd, Class<?> correspondingClass)
	{
		this.name = name;
		this.xsd = xsd;
		this.correspondingClass = correspondingClass;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public SqlDatatype getBaseType() {
		return null;
	}
	
	
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public Class<?> getCorrespondingClass() {
		return correspondingClass;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}



public class DatatypeSystemDefault
	implements DatatypeSystemOld
{
	public static final Logger logger = LoggerFactory.getLogger(DatatypeSystemDefault.class);

	public static SqlDatatype dt(String name) {
		return new SqlDatatypeImpl(name, null, null);
	}

	public static SqlDatatype dt(String name, Resource resource) {
		return new SqlDatatypeImpl(name, resource.asNode(), null);
	}

	public static SqlDatatype dt(String name, Resource resource, Class<?> clazz) {
		return new SqlDatatypeImpl(name, resource.asNode(), clazz);
	}
	
	public static final	SqlDatatype _BYTE = dt("byte", XSD.xboolean, Byte.class);	
	
	public static final SqlDatatype _SHORT = dt("short", XSD.xshort, Short.class);
	public static final SqlDatatype _INT = dt("int", XSD.xint, Integer.class);
	public static final SqlDatatype _LONG = dt("long", XSD.xlong, Long.class);
	public static final SqlDatatype _INTEGER = dt("integer", XSD.integer, Long.class);
	public static final SqlDatatype _DECIMAL = dt("decimal", XSD.decimal, BigDecimal.class);
	
	public static final SqlDatatype _UNSIGNED_BYTE = dt("unsigned_byte", XSD.unsignedByte, Short.class);
	public static final SqlDatatype _UNSIGNED_SHORT = dt("unsigned_short", XSD.unsignedShort, Integer.class);
	public static final SqlDatatype _UNSIGNED_INT = dt("unsigned_int", XSD.unsignedInt, Long.class);
	public static final SqlDatatype _UNSIGNED_LONG = dt("unsigned_long", XSD.unsignedLong, BigDecimal.class);
	public static final SqlDatatype _POSITIVE_INTEGER = dt("positive_integer", XSD.positiveInteger);
	public static final SqlDatatype _NON_NEGATIVE_INTEGER = dt("non_negative_integer", XSD.nonNegativeInteger);
	public static final SqlDatatype _NON_POSITIVE_INTEGER = dt("non_positive_integer", XSD.nonPositiveInteger);
	public static final SqlDatatype _NEGATIVE_INTEGER = dt("negative_integer", XSD.negativeInteger);

	public static final SqlDatatype _FLOAT = dt("float", XSD.xfloat, Float.class);
	public static final SqlDatatype _DOUBLE = dt("double", XSD.xdouble, Double.class);
	
	public static final SqlDatatype _NUMERIC = dt("numeric");


	public static final SqlDatatype _BOOLEAN = dt("boolean", XSD.xboolean, Boolean.class);
	public static final SqlDatatype _DATE = dt("date", XSD.date);
	public static final SqlDatatype _DATE_TIME = dt("datetime", XSD.dateTime, GregorianCalendar.class);
	//public static final SqlDatatype _TIMESTAMP = dt("timestamp", XSD.dateTime, GregorianCalendar.class);

	public static final SqlDatatype _STRING = dt("string", XSD.xstring, String.class);

	public static final SqlDatatype _GEOMETRY = dt("geometry");
	public static final SqlDatatype _GEOGRAPHY = dt("geography");

	public static final IBiSetMultimap<SqlDatatype, SqlDatatype> childToParent = new BiHashMultimap<SqlDatatype, SqlDatatype>();
	
		
	public static List<SqlDatatype> getDefaultDatatypes()
	{		
		List<SqlDatatype> result = new ArrayList<SqlDatatype>();
		for(Field field : DatatypeSystemDefault.class.getDeclaredFields()) {
			if(field.getType().equals(SqlDatatype.class)) {
				try {
					result.add((SqlDatatype)field.get(null));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return result;
	}
	
	static {
		childToParent.put(_BYTE, _SHORT);
		childToParent.put(_SHORT, _INT);
		childToParent.put(_INT, _LONG);
		childToParent.put(_LONG, _DECIMAL);
		childToParent.put(_DECIMAL, _INTEGER);

		childToParent.put(_UNSIGNED_BYTE, _UNSIGNED_SHORT);
		childToParent.put(_UNSIGNED_SHORT, _UNSIGNED_INT);
		childToParent.put(_UNSIGNED_INT, _UNSIGNED_LONG);
		childToParent.put(_UNSIGNED_LONG, _NON_NEGATIVE_INTEGER);
		childToParent.put(_NON_NEGATIVE_INTEGER, _INTEGER);
		
		childToParent.put(_NEGATIVE_INTEGER, _NON_POSITIVE_INTEGER);
		childToParent.put(_NON_POSITIVE_INTEGER, _INTEGER);
		
		childToParent.put(_INTEGER, _NUMERIC);
		childToParent.put(_FLOAT, _NUMERIC);
		childToParent.put(_DOUBLE, _NUMERIC);		
	}

	
	private Map<String, SqlDatatype> nameToDatatype = new HashMap<String, SqlDatatype>();
	private  IBiSetMultimap<SqlDatatype, SqlDatatype> typeHierarchy = new BiHashMultimap<SqlDatatype, SqlDatatype>();

	
	
	public SqlDatatype getByName(String name) {
		return nameToDatatype.get(name);
	}
	
	public SqlDatatype requireByName(String name) {
		SqlDatatype result = getByName(name);
		if(result == null) {
			throw new RuntimeException("No registered datatype found with name '" + name + "'");
		}
		
		return result;
	}
	
	/**
	 * Returns all registered datatype names.
	 * 
	 * This can be used to check whether in an external mapping
	 * a proper vendor specific name exists for each of them.
	 * 
	 * 
	 * @return
	 */
	public Set<String> getRegisteredDatatypes()
	{
		return nameToDatatype.keySet();
	}
	
	public void registerDatatype(String name, SqlDatatype datatype)
	{
		nameToDatatype.put(name, datatype);
	}
	
	public SqlDatatype getDatatype(String name) {
		return nameToDatatype.get(name);
	}
	
	public DatatypeSystemDefault() {
		// Create a copy of the defaults
		for(SqlDatatype item : DatatypeSystemDefault.getDefaultDatatypes()) {
			nameToDatatype.put(item.getName(), item);
			
			if(item.getXsd() != null) {
				nameToDatatype.put(item.getXsd().getURI(), item);
			}
		}
		
		for(Entry<SqlDatatype, SqlDatatype> entry : childToParent.entries()) {
			typeHierarchy.put(entry.getKey(), entry.getValue()); 
		}
		
		
	}
	

	/*
	public static Object cast(Object value, Class<?> datatype) {
	}
	*/

	//@Override
	public Object cast(Object value, SqlDatatype to) {
		Class<?> targetClazz = to.getCorrespondingClass();
		if(targetClazz == null) {
			//throw new RuntimeException("No class corresponding to '" + to + "' found.");
			logger.warn("No class corresponding to '" + to + "' found.");
			return null;
		}
		return Caster.tryCast(value, targetClazz);
	}


	@Override
	public Factory1<SqlExpr> cast(SqlDatatype from, SqlDatatype to) {

		
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SqlDatatype mostGenericDatatype(SqlDatatype from, SqlDatatype to) {
		throw new RuntimeException("This method does not make sense. We coulde always return object");
		// Maybe we could do a "backbone" structure?
	}

	@Override
	public Set<SqlDatatype> supremumDatatypes(SqlDatatype a, SqlDatatype b) {
		return MultiMaps.getCommonParent(childToParent.asMap(), a, b);
	}

	@Override
	public Integer compare(SqlDatatype a, SqlDatatype b) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main(String[] args) {
		DatatypeSystemOld system = new DatatypeSystemDefault();
		
		//System.out.println(system.supremumDatatypes(_UNSIGNED_SHORT, _BYTE));
		//System.out.println(system.supremumDatatypes(_INTEGER, _DOUBLE));
		//System.out.println(system.supremumDatatypes(_DOUBLE, _INTEGER));
	}

	@Override
	public SqlDatatype getByClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
		if(left.getDatatype() instanceof SqlDatatypeGeography && right.getDatatype() instanceof SqlDatatypeString)
			return SqlExprValue.FALSE;
	 */
}
