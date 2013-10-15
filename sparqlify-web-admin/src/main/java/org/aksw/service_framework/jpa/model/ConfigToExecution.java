package org.aksw.service_framework.jpa.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;


@Entity
public class ConfigToExecution {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String configClassName;
	
	@Lob
	private Serializable configId;
	private String configIdStr;
	
	private String executionContextClassName;
	
	@Lob
	private Serializable executionContextId;
	private String executionContextIdStr;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getConfigClassName() {
		return configClassName;
	}

	public void setConfigClassName(String configClassName) {
		this.configClassName = configClassName;
	}

	public Serializable getConfigId() {
		return configId;
	}

	public void setConfigId(Serializable configId) {
		this.configId = configId;
	}

	public String getConfigIdStr() {
		return configIdStr;
	}

	public void setConfigIdStr(String configIdStr) {
		this.configIdStr = configIdStr;
	}

	public String getExecutionContextClassName() {
		return executionContextClassName;
	}

	public void setExecutionContextClassName(String executionContextClassName) {
		this.executionContextClassName = executionContextClassName;
	}

	public Serializable getExecutionContextId() {
		return executionContextId;
	}

	public void setExecutionContextId(Serializable executionContextId) {
		this.executionContextId = executionContextId;
	}

	public String getExecutionContextIdStr() {
		return executionContextIdStr;
	}

	public void setExecutionContextIdStr(String executionContextIdStr) {
		this.executionContextIdStr = executionContextIdStr;
	}

	@Override
	public String toString() {
		return "ConfigToExecution [id=" + id + ", configClassName="
				+ configClassName + ", configId=" + configId + ", configIdStr="
				+ configIdStr + ", executionContextClassName="
				+ executionContextClassName + ", executionContextId="
				+ executionContextId + ", executionContextIdStr="
				+ executionContextIdStr + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configClassName == null) ? 0 : configClassName.hashCode());
		result = prime * result
				+ ((configId == null) ? 0 : configId.hashCode());
		result = prime * result
				+ ((configIdStr == null) ? 0 : configIdStr.hashCode());
		result = prime
				* result
				+ ((executionContextClassName == null) ? 0
						: executionContextClassName.hashCode());
		result = prime
				* result
				+ ((executionContextId == null) ? 0 : executionContextId
						.hashCode());
		result = prime
				* result
				+ ((executionContextIdStr == null) ? 0 : executionContextIdStr
						.hashCode());
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
		if (configClassName == null) {
			if (other.configClassName != null)
				return false;
		} else if (!configClassName.equals(other.configClassName))
			return false;
		if (configId == null) {
			if (other.configId != null)
				return false;
		} else if (!configId.equals(other.configId))
			return false;
		if (configIdStr == null) {
			if (other.configIdStr != null)
				return false;
		} else if (!configIdStr.equals(other.configIdStr))
			return false;
		if (executionContextClassName == null) {
			if (other.executionContextClassName != null)
				return false;
		} else if (!executionContextClassName
				.equals(other.executionContextClassName))
			return false;
		if (executionContextId == null) {
			if (other.executionContextId != null)
				return false;
		} else if (!executionContextId.equals(other.executionContextId))
			return false;
		if (executionContextIdStr == null) {
			if (other.executionContextIdStr != null)
				return false;
		} else if (!executionContextIdStr.equals(other.executionContextIdStr))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
