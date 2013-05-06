package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_ColumnRef
	extends SqlExprVarBase
{
	private String columnName;
	private String relationAlias;
	
	public S_ColumnRef(TypeToken datatype, String columnName) {
		this(datatype, columnName, null);
	}

	public S_ColumnRef(TypeToken datatype, String columnName, String relationAlias) {
		super(datatype);
		
		this.columnName = columnName;
		this.relationAlias = relationAlias;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getRelationAlias() {
		return relationAlias;
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print(getVarName());
	}

	@Override
	public String toString() {
		return getVarName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result
				+ ((relationAlias == null) ? 0 : relationAlias.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		S_ColumnRef other = (S_ColumnRef) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (relationAlias == null) {
			if (other.relationAlias != null)
				return false;
		} else if (!relationAlias.equals(other.relationAlias))
			return false;
		return true;
	}

	@Override
	public String getVarName() {
		String result;
		if(relationAlias == null) {
			result = "" + columnName;
		} else {
			result = relationAlias + "." + columnName;
		}
		
		return result;
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
