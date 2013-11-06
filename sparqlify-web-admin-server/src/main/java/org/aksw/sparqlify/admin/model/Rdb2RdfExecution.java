package org.aksw.sparqlify.admin.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Rdb2RdfExecution
{	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	//@Id
	private String name;

	private String status;
	
	//@ElementCollection
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<LogMessage> logMessages = new ArrayList<LogMessage>();

	// A reference to the configuration from which this execution was created
	//@ManyToOne
	@OneToOne
	private Rdb2RdfConfig config;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<LogMessage> getLogMessages() {
		return logMessages;
	}

	public void setLogMessages(List<LogMessage> logMessages) {
		this.logMessages = logMessages;
	}

	public Rdb2RdfConfig getConfig() {
		return config;
	}

	public void setConfig(Rdb2RdfConfig config) {
		this.config = config;
	}

	@Override
	public String toString() {
		return "Rdb2RdfExecution [id=" + id + ", name=" + name + ", status="
				+ status + ", logMessages=" + logMessages + ", config="
				+ config + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((logMessages == null) ? 0 : logMessages.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		Rdb2RdfExecution other = (Rdb2RdfExecution) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (logMessages == null) {
			if (other.logMessages != null)
				return false;
		} else if (!logMessages.equals(other.logMessages))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}	
}
