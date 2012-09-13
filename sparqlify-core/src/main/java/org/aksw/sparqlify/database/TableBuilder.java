package org.aksw.sparqlify.database;



public class TableBuilder<T> {
	//private Map<String, Class<?>> colums = new HashMap<String, Class<?>>();
	private IndexMap<String, Column> columns = new IndexMap<String, Column>();
	
	//public createTable()
	public void addColumn(String name, Class<? extends T> type) {
		columns.put(name, new Column(name, type));
	}
	
	
	public void clear()
	{
		columns.clear();
	}
	
	public Table<T> create() {
		TableImpl<T> result = new TableImpl<T>(columns);
		return result;
	}

	/*
	public void List<Row> find(Constraint ... constraints) {
		
	}*/
}
