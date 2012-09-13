package org.aksw.changesets;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.collections.diff.HashSetDiff;
import org.aksw.commons.collections.diff.IDiff;

import com.google.common.collect.Sets;

public class DiffPartitionMerger
{
	private File baseDir;
		
	private int numPartitions;
	
	public static FileLinePartition asFilePartition(Partition<String> partition) {
		if(partition instanceof CollectionDecorator) {
			Object decoratee = ((CollectionDecorator) partition).getDecoratee();
			
			if(decoratee instanceof FileLinePartition) {
				return (FileLinePartition) decoratee;
			}
		}
		
		return null;
	}
	
	
	public DiffPartitionMerger(File baseDir, int numPartitions) {
		this.numPartitions = numPartitions;
		this.baseDir = baseDir;
	}
	
	public IDiff<Map<Integer, Partition<String>>> merge(List<IDiff<File>> diffs) throws IOException {
		List<Diff<Map<Integer, Partition<String>>>> partitions = new ArrayList<Diff<Map<Integer, Partition<String>>>>();

		IDiff<Map<Integer, Partition<String>>> result = new Diff<Map<Integer, Partition<String>>>(
				new HashMap<Integer, Partition<String>>(),
				new HashMap<Integer, Partition<String>>(),
				null);

		
		Set<Integer> usedPartitions = new HashSet<Integer>();
		
		File tmpBaseDir = File.createTempFile("partition", "");
		
		int index = 0;
		for(IDiff<File> diff : diffs) {
			String tmpDir = tmpBaseDir.getAbsoluteFile() + "/" + index;
			
			Map<Integer, Partition<String>> a = Partitioner.partitionFile(diff.getAdded(), tmpDir, numPartitions);
			Map<Integer, Partition<String>> b = Partitioner.partitionFile(diff.getRemoved(), tmpDir, numPartitions);

			partitions.add(new Diff<Map<Integer, Partition<String>>>(a, b, null));
			
			usedPartitions.addAll(a.keySet());
			usedPartitions.addAll(b.keySet());
		
			++index;
		}
		
		
		for(Integer id : usedPartitions) {
			IDiff<Set<String>> mainDiff = new HashSetDiff<String>(); 
			for(Diff<Map<Integer, Partition<String>>> diff : partitions) {
				
				Partition<String> pa = diff.getAdded().get(id);
				Partition<String> pb = diff.getRemoved().get(id);

				Set<String> added = (pa == null) ? new HashSet<String>() : new HashSet<String>(pa);
				Set<String> removed = (pb == null) ? new HashSet<String>() : new HashSet<String>(pb);
				
				
				Set<String> intersectionA = new HashSet<String>(Sets.intersection(added, mainDiff.getRemoved()));
				Set<String> intersectionB = new HashSet<String>(Sets.intersection(removed, mainDiff.getAdded()));
				
				added.remove(intersectionA);
				mainDiff.getRemoved().remove(intersectionA);

				removed.removeAll(intersectionB);
				mainDiff.getAdded().removeAll(intersectionB);
				
				mainDiff.getAdded().addAll(added);
				mainDiff.getRemoved().addAll(removed);
			}
		
			if(mainDiff.getAdded().isEmpty() && mainDiff.getRemoved().isEmpty()) {
				continue;
			}
		
			
			Partition<String> added = getTargetPartition(result.getAdded(), id, "added");
			added.open();
			for(String item : mainDiff.getAdded()) {
				added.add(item);
			}
			added.flush();
			added.close();

			
			Partition<String> removed = getTargetPartition(result.getRemoved(), id, "removed");
			removed.open();
			for(String item : mainDiff.getRemoved()) {
				removed.add(item);
			}
			removed.flush();
			removed.close();
		}

	

		return result;
	}
		
	
	public Partition<String> getTargetPartition(Map<Integer, Partition<String>> map, int id, String suffix) {
		Partition<String> result = map.get(id);
		if(result == null) {
			
			File file = new File(baseDir.getAbsoluteFile() + "/" + suffix + "/" + id + ".part");

			file.getParentFile().mkdirs();

			
			result = new FileLinePartition(file);
			map.put(id, result);
		}

		return result;
	}

	
	public static void writePartitions(Map<Integer, Partition<String>> map, PrintStream printer) {		
		for(Partition<String> partition : map.values()) {
			for(String item : partition) {
				printer.println(item);
			}
		}		
	}

	public static void writePartitions(Map<Integer, Partition<String>> map, File file)
		throws FileNotFoundException
	{
		PrintStream printer = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
		
		writePartitions(map, printer);
		printer.flush();
		printer.close();
	}

}