package org.aksw.sparqlify.database;

import java.util.List;

public abstract class AbstractIndex
	implements Index
{
	private Table table;
	
	private int[] indexColumns;
	private List<String> columnNames;

	
	public AbstractIndex(Table table, List<String> columnNames, int[] indexColumns) {
		this.table = table;
		this.columnNames = columnNames;
		this.indexColumns = indexColumns;
	}
	
	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public int[] getIndexColumns() {
		return indexColumns;
	}

	@Override
	public List<String> getIndexColumnNames() {
		return columnNames;
	}

	@Override
	public boolean preAdd(List row) {
		return true;
	}

	@Override
	public void postAdd(List row) {
	}
}
