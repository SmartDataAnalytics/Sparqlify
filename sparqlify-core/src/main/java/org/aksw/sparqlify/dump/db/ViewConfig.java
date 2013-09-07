package org.aksw.sparqlify.dump.db;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ViewConfig {
	@Id
	private long id;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="dumpProcess")
	private List<ActivityDumpView> tasks;

	private String specFormat;
	private String specText; // The text of the specification used for the creation of the dump 

	private String properties; // Settings, mainly database connectivity, in the .properties format.

	public ViewConfig() {		
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<ActivityDumpView> getTasks() {
		return tasks;
	}

	public void setTasks(List<ActivityDumpView> tasks) {
		this.tasks = tasks;
	}

	public String getSpecFormat() {
		return specFormat;
	}

	public void setSpecFormat(String specFormat) {
		this.specFormat = specFormat;
	}

	public String getSpecText() {
		return specText;
	}

	public void setSpecText(String specText) {
		this.specText = specText;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
	
	// TODO Inherited attribute: State
}
