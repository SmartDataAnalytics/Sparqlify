package org.aksw.sparqlify.core;

import java.util.HashMap;
import java.util.HashSet;
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

import com.hp.hpl.jena.graph.Node;

public class DatatypeSystemCustom
	implements DatatypeSystem
{
	private static final Logger logger = LoggerFactory.getLogger(DatatypeSystemCustom.class);
	
	private Map<String, SqlDatatype> nameToType;
	private IBiSetMultimap<SqlDatatype, SqlDatatype> typeHierarchy;
	
	
	public DatatypeSystemCustom(Map<String, SqlDatatype> nameToType, IBiSetMultimap<SqlDatatype, SqlDatatype> typeHierarchy) {
		this.nameToType = nameToType;
		this.typeHierarchy = typeHierarchy;
	}


	@Override
	public SqlDatatype getByName(String name) {
		return nameToType.get(name);
	}


	@Override
	public SqlDatatype requireByName(String name) {
		SqlDatatype result = nameToType.get(name);
		if(result == null) {
			throw new RuntimeException("No registered datatype found with name '" + name + "'");
		}
		
		return result;
	}


	@Override
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
	}


	@Override
	public Set<SqlDatatype> supremumDatatypes(SqlDatatype from, SqlDatatype to) {
		return MultiMaps.getCommonParent(typeHierarchy.asMap(), from, to);
	}


	@Override
	public Integer compare(SqlDatatype a, SqlDatatype b) {
		// TODO Auto-generated method stub
		return null;
	}


	
	public static DatatypeSystemCustom create(Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy, Logger logger) {
		Set<String> all = new HashSet<String>();
		all.addAll(typeToClass.keySet());
		all.addAll(typeToUri.keySet());
		all.addAll(typeHierarchy.keySet());
		all.addAll(typeHierarchy.values());
		
		Map<String, SqlDatatype> nameToType = new HashMap<String, SqlDatatype>();
		
		for(String typeName : all) {
			String className = typeToClass.get(typeName);
			Class<?> clazz = null;

			if(className != null) {
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.error("Class '" + className + "' not found");
				}
			}
			
			String uri = typeToUri.get(typeName);
			Node node = null; 
			if(uri != null) {
				node = Node.createURI(uri);
			}
			
			SqlDatatype datatype = new SqlDatatypeImpl(typeName, node, clazz);
			
			nameToType.put(typeName, datatype);
		}
		
		IBiSetMultimap<SqlDatatype, SqlDatatype> subToSuperType = new BiHashMultimap<SqlDatatype,SqlDatatype>();
		
		for(Entry<String, String> entry : typeHierarchy.entrySet()) {
			
			SqlDatatype subType = nameToType.get(entry.getKey());
			SqlDatatype superType = nameToType.get(entry.getValue());

			subToSuperType.put(subType, superType);
		}
		

		DatatypeSystemCustom result = new DatatypeSystemCustom(nameToType, subToSuperType);
		return result;
	}
}
