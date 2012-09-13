package org.aksw.changesets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.aksw.commons.util.apache.ApacheLogDirectory;

public class FileLineIterable
	implements Iterable<String>
{
	public File file;
	
	public FileLineIterable(File file)
	{
		this.file = file;
	}
	
	@Override
	public Iterator<String> iterator() {
		try {
			InputStream base = ApacheLogDirectory.open(file);
			
			return new BufferedReaderLineIterator(new BufferedReader(new InputStreamReader(base)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}