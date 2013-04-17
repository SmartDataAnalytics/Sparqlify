package org.aksw.sparqlify.csv;

import java.io.BufferedReader;
import java.io.IOException;

public class ReaderSkipEmptyLines
	extends ReaderStringBase
{
	private BufferedReader reader;
	
	public ReaderSkipEmptyLines(BufferedReader reader) {
		this.reader = reader;
	}
	
	@Override
	protected String nextString() throws IOException {
		String tmp;
		do {
			tmp = reader.readLine();
		} while(tmp != null && tmp.isEmpty());
		
		//System.out.println("Returning line: " + result);
		String result;
		if(tmp == null) {
			result = null;
		} else {
			result = tmp + "\n";
		}		
	
		return result;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
