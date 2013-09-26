package org.aksw.sparqlify.admin.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Rdb2RdbConfig
	extends ResourceBase
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	private JdbcDataSource jdbcDataSource;
	
	@ManyToOne
	private TextResource textResource;
	
	public Rdb2RdbConfig() {
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public JdbcDataSource getJdbcDataSource() {
		return jdbcDataSource;
	}

	public void setJdbcDatasource(JdbcDataSource jdbcDataSource) {
		this.jdbcDataSource = jdbcDataSource;
	}

	public TextResource getTextResource() {
		return textResource;
	}

	public void setTextResource(TextResource textResource) {
		this.textResource = textResource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + id;
		result = prime * result
				+ ((jdbcDataSource == null) ? 0 : jdbcDataSource.hashCode());
		result = prime * result
				+ ((textResource == null) ? 0 : textResource.hashCode());
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
		Rdb2RdbConfig other = (Rdb2RdbConfig) obj;
		if (id != other.id)
			return false;
		if (jdbcDataSource == null) {
			if (other.jdbcDataSource != null)
				return false;
		} else if (!jdbcDataSource.equals(other.jdbcDataSource))
			return false;
		if (textResource == null) {
			if (other.textResource != null)
				return false;
		} else if (!textResource.equals(other.textResource))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rdb2RdbConfig [id=" + id + ", jdbcDatasource=" + jdbcDataSource
				+ ", textResource=" + textResource + "]";
	}
}
