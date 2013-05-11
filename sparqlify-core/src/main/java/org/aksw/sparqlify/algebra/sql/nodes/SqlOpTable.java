package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Set;

import org.openjena.atlas.io.IndentedWriter;

public class SqlOpTable
	extends SqlOpLeaf
{
	private String tableName;
	
	/** 
	 * The alias name is assigned automatically by the SqlOpSelectBlockCollector.
	 * If you think you need this field during the creation of algebraic expressions,
	 * you are probably doing something wrong; (at least I did so with the first version of Sparqlify)
	 */
	
	public SqlOpTable(Schema schema, String tableName) {
		this(schema, tableName, null);
	}

	public SqlOpTable(Schema schema, String tableName, String aliasName) {
		this(schema, tableName, aliasName, false);
	}

	public SqlOpTable(Schema schema, String tableName, String aliasName, boolean isEmpty) {
		super(schema, isEmpty, aliasName);
		this.tableName = tableName;
	}

	
	public String getTableName() {
		return tableName;
	}

	@Override
	public void write(IndentedWriter writer) {
		String aliasPart = aliasName == null ? " AS anonymous" : " AS " + aliasName;
		writer.print("SqlOpTable[" + tableName + aliasPart + "]");
	}

	@Override
	public String toString() {
		return "SqlOpTable [tableName=" + tableName + ", aliasName="
				+ aliasName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasName == null) ? 0 : aliasName.hashCode());
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
		SqlOpTable other = (SqlOpTable) obj;
		if (aliasName == null) {
			if (other.aliasName != null)
				return false;
		} else if (!aliasName.equals(other.aliasName))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	@Override
	public String getId() {
		return tableName;
	}
	
	public SqlOp copy(Schema schema, List<SqlOp> newArgs) {
		SqlOpTable result = new SqlOpTable(schema, tableName, aliasName, isEmpty);
		return result;
	}

}
