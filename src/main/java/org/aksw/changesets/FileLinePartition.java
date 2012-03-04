package org.aksw.changesets;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class FileLinePartition
	extends AbstractPartition<String>
{
	private File file;
	private PrintStream out;
	private int size;
		
	public FileLinePartition(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public void open()
	{
		if(out != null) {
			throw new IllegalStateException();
		}
		
		try {
			this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean add(String item) {
		out.println(item);
		size += 1;
		
		return true;
	}

	@Override
	public void close() {
		out.flush();
		out.close();
		
		out = null;
	}
	
	public int size() {
		return size;
	}
	
	
	/*
	public Set<String> getData() {
		Set<String> result = new HashSet<String>(IterableCollection.wrap(new FileLineIterable(file)));

		return result;
	}
	*/
	
	/**
	 * Sets the data for the partition
	 * 
	 * @param data
	 * /
	public void setData(Set<String> data) {
		if(out != null) {
			throw new IllegalStateException();
		}
		
		try {
			this.out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		for(String item : data) {
			out.println(item);
		}
		
		out.flush();
		out.close();
		out = null;
		
		this.size = data.size();
	}*/

	@Override
	public Iterator<String> iterator() {
		return new FileLineIterable(file).iterator();
	}

	@Override
	public void flush() {
		out.flush();
	}
}