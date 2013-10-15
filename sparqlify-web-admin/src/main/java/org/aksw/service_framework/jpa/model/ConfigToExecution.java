package org.aksw.service_framework.jpa.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class ConfigToExecution {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private Serializable configId;
	private Serializable executionId;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Serializable getConfigId() {
		return configId;
	}
	public void setConfigId(Serializable configId) {
		this.configId = configId;
	}
	public Serializable getExecutionId() {
		return executionId;
	}
	public void setExecutionId(Serializable executionId) {
		this.executionId = executionId;
	}
	@Override
	public String toString() {
		return "ConfigToExecution [id=" + id + ", configId=" + configId
				+ ", executionId=" + executionId + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configId == null) ? 0 : configId.hashCode());
		result = prime * result
				+ ((executionId == null) ? 0 : executionId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ConfigToExecution other = (ConfigToExecution) obj;
		if (configId == null) {
			if (other.configId != null)
				return false;
		} else if (!configId.equals(other.configId))
			return false;
		if (executionId == null) {
			if (other.executionId != null)
				return false;
		} else if (!executionId.equals(other.executionId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
