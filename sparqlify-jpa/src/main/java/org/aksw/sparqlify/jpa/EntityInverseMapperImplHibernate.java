package org.aksw.sparqlify.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMap;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapper;
import org.aksw.sparqlify.util.SqlOpUtils;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.hp.hpl.jena.sparql.core.Quad;


public class EntityInverseMapperImplHibernate
	implements EntityInverseMapper
{
	private SparqlSqlInverseMapper inverseMapper;

	private Map<String, AbstractEntityPersister> tableNameToPersister;

	
	public EntityInverseMapperImplHibernate(SparqlSqlInverseMapper inverseMapper, Map<String, AbstractEntityPersister> tableNameToPersister) {
		this.inverseMapper = inverseMapper;
		this.tableNameToPersister = tableNameToPersister;
	}
	
	@Override
	public EntityRef map(Quad quad) {
		List<SparqlSqlInverseMap> invMaps = inverseMapper.map(quad);
		
		for(SparqlSqlInverseMap invMap : invMaps) {
			map(invMap);
		}

		return null;
	}

	public EntityRef map(SparqlSqlInverseMap invMap) {
		EntityRef result = map(invMap, tableNameToPersister);
		return result;
	}

	

	
	public static EntityRef map(SparqlSqlInverseMap invMap, Map<String, AbstractEntityPersister> metadata) {
		String tableName = SqlOpUtils.getTableName(invMap.getViewDefinition().getMapping().getSqlOp());
		Map<String, Object> columnToValue = makeSimple(invMap.getColumnToValue());
		
		EntityRef result = map(tableName, columnToValue, metadata);
		return result;
	}
	
//	public static EntityInverseMapperImplHibernate create(SparqlSqlInverseMapper inverseMapper, EntityManagerFactory emf) {
//		SessionFactory sessionFactory = ((HibernateEntityManagerFactory)emf).getSessionFactory();

	public static EntityInverseMapperImplHibernate create(SparqlSqlInverseMapper inverseMapper, SessionFactory sessionFactory) {
		Map<String, AbstractEntityPersister> tableNameToPersister = createTablePersisterMap(sessionFactory);
		
		
		EntityInverseMapperImplHibernate result = new EntityInverseMapperImplHibernate(inverseMapper, tableNameToPersister);
		return result;
	}		
	
	
	public static Map<String, Object> makeSimple(Map<S_ColumnRef, SqlValue> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		for(Entry<S_ColumnRef, SqlValue> entry : map.entrySet()) {
			S_ColumnRef colRef = entry.getKey();
			SqlValue sqlValue = entry.getValue();
			
			String columnName = colRef.getColumnName();
			Object value = sqlValue.getValue();
			
			result.put(columnName, value);
		}
		
		return result;
	}
	
	public static EntityRef map(String tableName, Map<String, Object> columnConstraints, Map<String, AbstractEntityPersister> metadata) {
		AbstractEntityPersister persister = metadata.get(tableName);
		for(Entry<String, Object> constraint : columnConstraints.entrySet()) {
			String columnName = constraint.getKey();
			Object value = constraint.getValue();
			
			System.out.println("TODO How to deal with compound keys? How to map column names to properties? Probably we have to iterate all the properties and see whether the columns contribute to it");
			//persister.getPropert
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * 
	 * @param sessionFactory
	 * @return A collection of AbstractEntityPersister objects indexed by their table name
	 */
	public static Map<String, AbstractEntityPersister> createTablePersisterMap(SessionFactory sessionFactory) {

		Map<String, AbstractEntityPersister> result = new HashMap<String, AbstractEntityPersister>();

		Collection<ClassMetadata>classMetadatas = sessionFactory.getAllClassMetadata().values();
		for(ClassMetadata classMetadata : classMetadatas) {
			
			if (!(classMetadata instanceof AbstractEntityPersister)) {
				continue;
			}			
			
			AbstractEntityPersister persister = (AbstractEntityPersister)classMetadata;
			
			String tableName = persister.getTableName();
			result.put(tableName, persister);
		}
		
		return result;
	}
}
