package org.aksw.sparqlify.dump.db;

import javax.persistence.Entity;

@Entity
public class DumpPartitionState {
	private DumpPartition dumpPartition;
	private String targetFileName;
	private long rowId;
	private long filePos;
}
