package org.aksw.changesets;

import java.io.BufferedReader;
import java.io.IOException;

import org.aksw.commons.collections.SinglePrefetchIterator;

public class BufferedReaderLineIterator
	extends SinglePrefetchIterator<String>
{
	private BufferedReader reader;
	
	public BufferedReaderLineIterator(BufferedReader reader)
	{
		this.reader = reader;
	}

	@Override
	protected String prefetch()
		throws Exception
	{
		String line = reader.readLine();
		
		if(line == null) {
			return finish();
		} else {
			return line;
		}
	}
	
	@Override
	public void close()
	{
		try {
			if(reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}