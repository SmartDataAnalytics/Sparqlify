package org.aksw.changesets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.collections.diff.IDiff;
import org.aksw.commons.util.numbers.NumberChunker;

public class ChangesetRepository
{
	private long minimumId = -1;
	private File repositoryRoot;
	private long nextId;

	
	// The layout on how to partition the ids
	private List<Long> layout;
	
	private Object formatter;
	
	
	public ChangesetRepository(File repositoryRoot) {
		layout = new ArrayList<Long>();
		
		layout.add(1000l);
		layout.add(1000l);
		
		this.repositoryRoot = repositoryRoot;
		
		load();
	}
	
	public long setNextId()
	{
		return nextId;
	}
	
	private void load() {
		File base = repositoryRoot;
		List<Long> chunks = new ArrayList<Long>();
		for(;;) {
			Entry<Long, File> next = getDirectories(base).firstEntry();
			if(next == null) {
				break;
			}
			
			chunks.add(next.getKey());
			
			base = next.getValue();
		}
		
		
		Entry<Long, IDiff<File>> entry = getFiles(base).firstEntry();
		if(entry == null) {
			throw new RuntimeException("Could not find a file in the repo. Make sure you do not have empty directories.");
		}
		chunks.add(entry.getKey());
		
		this.minimumId = generateId(chunks, layout);
	}
	
	public static long generateId(List<Long> values, List<Long> layout)
	{
		if(!((values.size() == layout.size()) || (values.size() == layout.size() + 1))) {
			throw new RuntimeException("Sizes are different");
		}
		
		long result = 0;
		long base = 1;
		for(int i = 0; i < values.size(); ++i) {
			result += values.get(i) * base;
			
			if(i < layout.size()) {
				base *= layout.get(i);
			}
		}
	
		return result;
	}
	
	
	public static Long getIdFromDirectory(File dir) {
		Long result = null;
		try {
			result = Long.parseLong(dir.getName());
		} catch(Throwable e) {
			
		}

		return result;
	}
	
	public static Long getIdFromFile(File file) {
		Long result = null;
		String name = file.getName();

		int dotIndex = name.indexOf('.');
		if(dotIndex > 0) {
			String part = name.substring(0, dotIndex);
			try {						
				result = Long.parseLong(part);
			} catch(Throwable e) {
				
			}
		}

		return result;
	}
	
	
	public static NavigableMap<Long, File> getDirectories(File dir)
	{		
		NavigableMap<Long, File> result = new TreeMap<Long, File>();

		for(File file : dir.listFiles()) {
			if(!file.isDirectory()) {
				continue;
			}
	
			Long id = getIdFromDirectory(file);
						
			if(id != null) {
				result.put(id, file);
			}
		}
		
		return result;
	}
	
	/**
	 * Creates a copy of a diff object with the 'added' property set
	 * 
	 * 
	 * @param <T>
	 * @param diff
	 * @param added
	 * @return
	 */
	public static <T> IDiff<T> setAdded(IDiff<T> diff, T added) {
		if(diff.getAdded() != null) {
			throw new RuntimeException("Conflict between '" + diff.getAdded() + "' and '" + added + "'");
		}
		
		return new Diff<T>(added, diff.getRemoved(), diff.getRetained());
	}
	
	public static <T> IDiff<T> setRemoved(IDiff<T> diff, T removed) {
		if(diff.getAdded() != null) {
			throw new RuntimeException("Conflict between '" + diff.getRemoved() + "' and '" + removed + "'");
		}
		
		return new Diff<T>(removed, diff.getRemoved(), diff.getRetained());
	}
	
	
	
	public static NavigableMap<Long, IDiff<File>> getFiles(File dir)
	{
		NavigableMap<Long, IDiff<File>> result = new TreeMap<Long, IDiff<File>>();

		for(File file : dir.listFiles()) {
			if(!file.isFile()) {
				continue;
			}

			Long id = getIdFromFile(file);

			if(id != null) {
				IDiff<File> diff = result.get(id);

				
				File added = null;
				File removed = null;
				
				if(file.getName().contains("added")) {
					added = file;
				} else if(file.getName().contains("removed")) {
					removed = file;
				} else {
					continue;
				}
				
				if(diff == null) {
				
					diff = new Diff<File>(added, removed, null);
				
				} else if(added != null) {
					
					if(diff.getAdded() != null) {
						throw new RuntimeException("Conflict between '" + diff.getAdded() + "' and '" + added + "'");
					}					
					diff = new Diff<File>(added, diff.getRemoved(), diff.getRetained());
				
				} else if(removed != null) {

					if(diff.getRemoved() != null) {
						throw new RuntimeException("Conflict between '" + diff.getRemoved() + "' and '" + removed + "'");
					}					
					diff = new Diff<File>(diff.getAdded(), removed, diff.getRetained());
					
				} else {
					throw new RuntimeException("Should not happen");
				}
				
				
				result.put(id, diff);
			}
		}

		return result;
	}
	
	
	
	
	public long getMinimumId()
	{
		return minimumId;
	}
	
	
	/*
	public long getSuccessorId(long id) {
		
	}*/
	
	public void getNextId() 
	{
		//return nextId;
	}
	
	/**
	 * Get the changeset with the specified id
	 *
	 * @param id
	 * @return
	 */
	/*
	public IDiff<File> get(long id)
	{
		
	}*/
	

	private String format(long id)
	{
		String result = Long.toString(id);
		if (id < 100)
			result = "0" + result;

		if (id < 10)
			result = "0" + result;

		return result;
	}

	String getFragment(long id)
	{
		List<Long> parts = NumberChunker.chunkValue(id, 1000, 1000);

		String fragment = ""; // Long.toString(parts.get(0));
		for (Long part : parts) {
			fragment += "/" + format(part);
		}

		return fragment;
	}

}