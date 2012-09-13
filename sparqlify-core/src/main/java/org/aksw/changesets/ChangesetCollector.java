package org.aksw.changesets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.aksw.commons.collections.diff.IDiff;
import org.aksw.commons.collections.iterators.DescenderIterator;
import org.aksw.commons.util.Pair;


public class ChangesetCollector
{
	public static void main(String[] args) throws IOException 
	{	
		File repoPath = new File("/home/raven/Desktop/LinkedGeoData/changesets");
		ChangesetRepository repo = new ChangesetRepository(repoPath);
		
		long id = repo.getMinimumId();
		
		System.out.println(id);
	
		Entry<Long, File> base = Pair.create(0l, repoPath);
		DescenderIterator<Entry<Long, File>> it = new DescenderIterator<Entry<Long,File>>(base, new RepositoryDescender());


		int test = 0;
		
        while (it.hasNext()) {

        	List<Entry<Long, File>> current = it.peek();

            if(!isAccepted(current)) {
                it.next();
                continue;
            }
        	System.out.println("Accepted Path: " + current);        	
        	
            
            if(!it.canDescend()) {
            	
            	System.out.println("Now doing something");
            	File directory = current.get(current.size() - 1).getValue();
            	
            	NavigableMap<Long, IDiff<File>> files = ChangesetRepository.getFiles(directory);
            	
            	List<IDiff<File>> f = new ArrayList<IDiff<File>>();
            	for(Entry<Long, IDiff<File>> item : files.entrySet()) {
            		//System.out.println(item.getKey() + ": " + item.getKey() + " - " + item.getValue());
            		f.add(item.getValue());
            	}

            	DiffPartitionMerger merger = new DiffPartitionMerger(new File("/home/raven/Desktop/LinkedGeoData/merge/"), 1000);
            	IDiff<Map<Integer, Partition<String>>> partitions = merger.merge(f);
    
            	File file = new File("/home/raven/Desktop/LinkedGeoData/merge/" + test + ".added.nt");
            	DiffPartitionMerger.writePartitions(partitions.getAdded(), file);
            	
            	file = new File("/home/raven/Desktop/LinkedGeoData/merge/" + test + ".removed.nt");
            	DiffPartitionMerger.writePartitions(partitions.getRemoved(), file);

            	
            	++test;
            	
            	//System.out.println(current);
            	//System.out.println("got a candidate");
            	it.next();
            }
            else {
            	it.descend();
            }
        }
	}
	
	public static boolean isAccepted(Object o) {
		return true;
	}
}	

