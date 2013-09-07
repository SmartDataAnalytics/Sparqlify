package org.aksw.sparqlify.dump.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class ActivityDumpView {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@ManyToOne
	private ViewConfig dumpProcess;
	
	private String viewFormat;
	private String viewText;
	
	
	/*
	 * The default number of rows to process in a partition. 
	 */
	//private long defaultPartitionSize;
	
	private Long estimatedRowCount; // The estimated number of rows in the views underlying relation

	public ActivityDumpView() {
		
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ViewConfig getDumpProcess() {
		return dumpProcess;
	}

	public void setDumpProcess(ViewConfig dumpProcess) {
		this.dumpProcess = dumpProcess;
	}

	public String getViewFormat() {
		return viewFormat;
	}

	public void setViewFormat(String viewFormat) {
		this.viewFormat = viewFormat;
	}

	public String getViewText() {
		return viewText;
	}

	public void setViewText(String viewText) {
		this.viewText = viewText;
	}

	public Long getEstimatedRowCount() {
		return estimatedRowCount;
	}

	public void setEstimatedRowCount(Long estimatedRowCount) {
		this.estimatedRowCount = estimatedRowCount;
	}
}
