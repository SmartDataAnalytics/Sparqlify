package org.aksw.sparqlify.dump.db;

import java.util.Set;

/**
 * Represents a dataset that is based on
 * database connection settings and
 * a view definition config
 *
 * 
 * @author raven
 *
 */
public class DatasetViewConfig
	extends Dataset
{
	private Activity activity; // The activity the live of this object is attached to
	
	private DumpDbConfig dbConfig;
	private ViewConfig viewConfig;
	
	private boolean isExclusive;
	private Set<String> viewNames;
	

}
