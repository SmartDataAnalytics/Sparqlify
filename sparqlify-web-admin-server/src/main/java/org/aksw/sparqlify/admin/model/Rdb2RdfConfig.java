package org.aksw.sparqlify.admin.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Rdb2RdfConfig
	extends ResourceBase
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	// The context path on which to host the SPARQL services
	private String contextPath;

	private Integer maxResultSetRows;
	private Integer maxExecutionTimeInSeconds;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private JdbcDataSource jdbcDataSource;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private TextResource textResource;
	
	public Rdb2RdfConfig() {
		
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public JdbcDataSource getJdbcDataSource() {
		return jdbcDataSource;
	}

	public void setJdbcDataSource(JdbcDataSource jdbcDataSource) {
		this.jdbcDataSource = jdbcDataSource;
	}

	public TextResource getTextResource() {
		return textResource;
	}

	public void setTextResource(TextResource textResource) {
		this.textResource = textResource;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public Integer getMaxResultSetRows() {
		return maxResultSetRows;
	}

	public void setMaxResultSetRows(Integer maxResultSetRows) {
		this.maxResultSetRows = maxResultSetRows;
	}

	public Integer getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(Integer maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((contextPath == null) ? 0 : contextPath.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((jdbcDataSource == null) ? 0 : jdbcDataSource.hashCode());
		result = prime
				* result
				+ ((maxExecutionTimeInSeconds == null) ? 0
						: maxExecutionTimeInSeconds.hashCode());
		result = prime
				* result
				+ ((maxResultSetRows == null) ? 0 : maxResultSetRows.hashCode());
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
		Rdb2RdfConfig other = (Rdb2RdfConfig) obj;
		if (contextPath == null) {
			if (other.contextPath != null)
				return false;
		} else if (!contextPath.equals(other.contextPath))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (jdbcDataSource == null) {
			if (other.jdbcDataSource != null)
				return false;
		} else if (!jdbcDataSource.equals(other.jdbcDataSource))
			return false;
		if (maxExecutionTimeInSeconds == null) {
			if (other.maxExecutionTimeInSeconds != null)
				return false;
		} else if (!maxExecutionTimeInSeconds
				.equals(other.maxExecutionTimeInSeconds))
			return false;
		if (maxResultSetRows == null) {
			if (other.maxResultSetRows != null)
				return false;
		} else if (!maxResultSetRows.equals(other.maxResultSetRows))
			return false;
		if (textResource == null) {
			if (other.textResource != null)
				return false;
		} else if (!textResource.equals(other.textResource))
			return false;
		return true;
	}
	
}
