package org.aksw.sparqlify.csv;

import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Joiner;

/**
 * Wraps a opencsv CSVReader as a java Reader object.
 * User for passing the data into H2 CSV utils.
 * 
 * @author raven
 *
 */
public class ReaderCSVReader
	extends ReaderStringBase
{
	private static final String newLine = "\n";
	private static final Joiner joiner = Joiner.on("\",\"");

	private CSVReader csvReader;
	//CSVWriter writer = new CSVWriter(


	public ReaderCSVReader(CSVReader csvReader) {
		this.csvReader = csvReader;
	}
	
	public static String encodeCell(String cell) {
		String result = cell.replace("\"", "\\\"");
		return result;
	}
	
	public static String[] encodeCells(String[] cells) {
		String[] result = new String[cells.length];
		for(int i = 0; i < cells.length; ++i) {
			String rawCell = cells[i];
			result[i] = encodeCell(rawCell);
		}
		
		return result;
	}
	
	public static String createLine(String[] cells) {
		String[] encodedCells = encodeCells(cells);
		String result = "\"" + joiner.join(encodedCells) + "\"" + newLine;
		return result;
	}
	
	@Override
	protected String nextString() {
		try {
			String[] strs = csvReader.readNext();
			
			String result;
			if(strs == null) {
				result = null;
			} else if(strs.length == 0) {
				result = newLine;
			} else if(strs.length == 1) {
				String str = strs[0];
				if(str.isEmpty()) {
					result = newLine;
				} else {
					result = createLine(strs); 
				}
			}
			else {
				result = createLine(strs);
			}

			System.out.println(result);
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	@Override
	public void close() throws IOException {
		csvReader.close();
	}
}