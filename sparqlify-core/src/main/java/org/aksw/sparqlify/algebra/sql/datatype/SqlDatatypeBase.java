package org.aksw.sparqlify.algebra.sql.datatype;

import org.aksw.sparqlify.core.SqlDatatype;

import com.hp.hpl.jena.graph.Node;


public class SqlDatatypeBase
	implements SqlDatatype
{
	private int sqlType;

	protected SqlDatatypeBase(int sqlType) {
		this.sqlType = sqlType;
	}
	
	public int getSqlType() {
		return sqlType;
	}
	
	
	@Override
	public int hashCode() {
		return sqlType * 97 * this.getClass().hashCode();
	}

	@Override
	public SqlDatatype getBaseType() {
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getCorrespondingClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getXsd() {
		// TODO Auto-generated method stub
		return null;
	}
}
