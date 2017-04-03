package org.aksw.sparqlify.jpa;

import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.sparql.core.Quad;


public class EntityInverseMapperImplHibernate
    implements EntityInverseMapper
{
    private static final Logger logger = LoggerFactory.getLogger(EntityInverseMapperImplHibernate.class);

    private SparqlSqlInverseMapper inverseMapper;

    private Map<String, AbstractEntityPersister> tableNameToPersister;


    public EntityInverseMapperImplHibernate(SparqlSqlInverseMapper inverseMapper, Map<String, AbstractEntityPersister> tableNameToPersister) {
        this.inverseMapper = inverseMapper;
        this.tableNameToPersister = tableNameToPersister;
    }


    @Override
    public List<EntityRef> map(Quad quad) {
        List<SparqlSqlInverseMap> invMaps = inverseMapper.map(quad);

        List<EntityRef> result = new ArrayList<EntityRef>(invMaps.size());

        for(SparqlSqlInverseMap invMap : invMaps) {
            EntityRef entityRef = map(invMap);
            if(entityRef != null) {
                result.add(entityRef);
            }
        }

        return result;
    }

    public EntityRef map(SparqlSqlInverseMap invMap) {
        EntityRef result = map(invMap, tableNameToPersister);
        return result;
    }




    public static EntityRef map(SparqlSqlInverseMap invMap, Map<String, AbstractEntityPersister> metadata) {
        String tableName = SqlOpUtils.getTableName(invMap.getViewDefinition().getMapping().getSqlOp());

        EntityRef result = null;
        if(tableName != null) {
            Map<String, Object> columnToValue = makeSimple(invMap.getColumnToValue());

            result = map(tableName, columnToValue, metadata);
        }

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

        if(persister == null) {
            return null;
        }

        //TODO Only works on entity ids yet, however should be extended to work on arbitrary properties

        Map<String, String> columnToProperty = new HashMap<String, String>();



        String[] propertyNames = persister.getPropertyNames();

        for(String propertyName : propertyNames) {
            //String propertyTableName = persister.getPropertyTableName(propertyName);

            //System.out.println(propertyTableName);

            String[] columnNames = persister.getPropertyColumnNames(propertyName);
            if(columnNames.length > 1 || columnNames.length == 0) {
                logger.warn("Skipped property with multi column mapping: " + propertyName + " -> " + columnNames);
                // Skip multi column properties for now
                continue;
            }

            String columnName = columnNames[0];

            // TODO Non-portable code (cross-dbms) I don't get how to get the final SQL column names from hibernate :/
            // The ones I get here are camel case, where as in the db they are all lower case
            columnName = columnName.toLowerCase();

            columnToProperty.put(columnName, propertyName);

        }


        // Set the id property (may override a prior mapping)
        // TODO This code may not be robust
        String idPropertyName = persister.getIdentifierPropertyName();
        String[] idColumnNames = persister.getIdentifierColumnNames();
        if (idColumnNames.length == 1) {
            String idColumnName = idColumnNames[0];
            columnToProperty.put(idColumnName, idPropertyName);
        }



        Map<String, Object> propertyToValue = new HashMap<String, Object>();

        boolean allMatch = true;
        for(Entry<String, Object> constraint : columnConstraints.entrySet()) {
            String columnName = constraint.getKey();
            Object value = constraint.getValue();


            String propertyName = columnToProperty.get(columnName);
            if(propertyName == null) {
                allMatch = false;
                break;
            }

            propertyToValue.put(propertyName, value);

            //System.out.println("TODO How to deal with compound keys? How to map column names to properties? Probably we have to iterate all the properties and see whether the columns contribute to it");
            //persister.getPropert
        }

        Class<?> entityClass = persister.getMappedClass();
        EntityRef result = allMatch ? new EntityRef(entityClass, propertyToValue) : null;

        return result;
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

            String tmpTableName = persister.getTableName();
            String tableName = tmpTableName.toLowerCase();
            logger.warn("[HACK] Converted table name '" + tmpTableName  +"' to '" + tableName + "', but this should be done via the SQL dialect or something");
            //tableName = tableName.toLowerCase();

            result.put(tableName, persister);
        }

        return result;
    }

    /**
     * Look up an instance of a class by a URI
     *
     * TODO We would need access to the candidate selector here, in order to pick the view definitions for a specific table
     *
     *
     * @param clazz
     * @param uri
     * @return
     */
    /*
    public <T> T find(Class<T> clazz, String uri) {

        for(AbstractEntityPersister persister : tableNameToPersister.values()) {
            Class<?> mappedClazz = persister.getMappedClass();
            if(clazz.isAssignableFrom(mappedClazz)) {

                inverseMapper.map(quad)
            }


        }

        ClassMetadata classMetadata = sessionFactory.getClassMetadata(entityClass);
        //classMetadata.

        return null;
    }
    */

}
