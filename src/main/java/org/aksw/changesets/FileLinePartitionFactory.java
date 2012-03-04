package org.aksw.changesets;

import java.io.File;

public class FileLinePartitionFactory
	implements PartitionFactory<String>
{
	private File file;
	
	public FileLinePartitionFactory(File file)
	{
		this.file = file;
	}

	@Override
	public Partition<String> create() {
		return new FileLinePartition(file);
	}
}