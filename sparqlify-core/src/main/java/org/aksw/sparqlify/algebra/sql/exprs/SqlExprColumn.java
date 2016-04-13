package org.aksw.sparqlify.algebra.sql.exprs;


import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprVisitor;
import org.aksw.sparqlify.core.SqlDatatype;
import org.apache.jena.atlas.io.IndentedWriter;


public class SqlExprColumn
	extends SqlExpr0
{
	private String tableName;
	private String columnName;
	
	org.apache.jena.sdb.core.sqlnode.SqlTable jenaTable = null;
	
	public SqlExprColumn(String tableName, String columnName, SqlDatatype datatype) {
		super(datatype);

		this.tableName = tableName;
		this.columnName = columnName;

		
		if(tableName != null) {
			jenaTable = new org.apache.jena.sdb.core.sqlnode.SqlTable(tableName);	
		}
	}
	
	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}


	public String getFullColumnName() {
		if(tableName == null) {
			return columnName;
		} else {
			return tableName + "." + columnName;
		}
	}

	//@Override
	public boolean isColumn()
	{
		return true;
	}

    public String asString()
	{
		return getFullColumnName();
	}
    
    public void visit(SqlExprVisitor visitor) {
    	throw new RuntimeException("Something was messed up");
    	//visitor.visit(new SqlColumn(jenaTable, columnName, null)) ;
    }

	
    /*
	@Override
	public String toString()
	{
		return asSQL();
	}*/



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlExprColumn other = (SqlExprColumn) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	@Override
	public void asString(IndentedWriter writer) 
	{
		writer.print((tableName == null ? "" : tableName + ".") + columnName);
		//asString(writer, this.getClass().getSimpleName(), getArgs());
	}

	
	@Override
	public String toString() 
	{
		return tableName + "." + columnName;
		//return asString();
	}
}
