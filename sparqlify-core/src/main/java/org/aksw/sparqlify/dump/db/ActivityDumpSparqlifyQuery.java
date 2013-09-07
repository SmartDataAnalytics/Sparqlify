package org.aksw.sparqlify.dump.db;

import java.util.Set;

import javax.persistence.Entity;


/**
 * The state for writing a the result of a query to a file.
 * 
 * TODO I am not totally sure about how the model works...
 * There are activities that have a state and may be sub activites of other ones.
 * So sub activities need to have access to the parent activity.
 * And actually, sub activities need to be interpreted in the parent's context.
 * 
 * 
 * @author raven
 *
 */
@Entity
public class ActivityDumpSparqlifyQuery {
	private DatasetViewConfig dataset;
	
	private Activity parent; // The parent activity the life time of this task depends on.
	
	private DumpDbConfig dbConfig;
	private ViewConfig viewConfig;
	
	private boolean isExclusive;
	private Set<String> viewNames;

	
	private String queryString;
	private String targetFileName;
		
	// State when the file was last flushed to disk, initally 0
	private long fileOffset;
	private long queryOffset; // Limit/Offset to use on the query
	
	// S
	//private 
	
	public ActivityDumpSparqlifyQuery() {
	}
	
	public Void getTask() {
		return null;
	}
	
	public String getQueryString() {
		return this.queryString;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	
	public void setTask(Void task) {
		//this.task = task;
	}

	public String getTargetFileName() {
		return targetFileName;
	}

	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}

	public long getFileOffset() {
		return fileOffset;
	}

	public void setFileOffset(long fileOffset) {
		this.fileOffset = fileOffset;
	}

	public long getQueryOffset() {
		return queryOffset;
	}

	public void setQueryOffset(long queryOffset) {
		this.queryOffset = queryOffset;
	}
}
