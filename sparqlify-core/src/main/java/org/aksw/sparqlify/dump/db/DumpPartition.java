package org.aksw.sparqlify.dump.db;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parent;

public class DumpPartition {
	
	@GeneratedValue(strategy=GenerationType.AUTO)
    //@GenericGenerator(name="identity", strategy="identity")
	private long id;

	@Parent
	private ActivityDumpView dumpTask;

	
	private Long offset;
	private Long limit;
	
}
