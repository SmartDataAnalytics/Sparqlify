package org.aksw.sparqlify.database;

import java.util.List;

public interface MetaIndexFactory
{
	MapStoreAccessor create(Table table, List<String> columnNames);
	
}