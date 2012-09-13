package org.aksw.changesets;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Partitioner
{
	
	public static Map<Integer, Partition<String>> partitionFile(File sourceFile, String tmpDir, int numPartitions)
	{
		Map<Integer, Partition<String>> result = new HashMap<Integer, Partition<String>>();
		
		// Partition the source file
		for(String line : new FileLineIterable(sourceFile)) {
			int rawHash = line.hashCode();
			int normalizedHash = rawHash % numPartitions;
			
			Partition<String> partition = result.get(normalizedHash);
			if(partition == null) {
				File file = new File(tmpDir + "/" + normalizedHash + ".part");

				file.getParentFile().mkdirs();
				
				partition = new BackedPartition<String>(new FileLinePartitionFactory(file));
				partition.open();
				
				result.put(normalizedHash, partition);
			}
			
			partition.add(line);
		}
		
		for(Partition<String> partition : result.values()) {
			partition.flush();
			partition.close();
		}
	
		return result;
	}
}