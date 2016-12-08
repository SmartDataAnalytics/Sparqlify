package org.aksw.sparqlify.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.apache.commons.collections15.Transformer;

import au.com.bytecode.opencsv.CSVReader;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;


class CellToStringTransformer
	implements Transformer<Cell, String>
{
	private static CellToStringTransformer instance = null;
	
	public static CellToStringTransformer getInstance() {
		if(instance == null) {
			instance = new CellToStringTransformer();
		}
		
		return instance;
	}
	
	
	@Override
	public String transform(Cell input) {
		return input.getContents();
	}
}


class CsvRowIterator
	extends SinglePrefetchIterator<List<String>>
{
	private CSVReader reader;
	private boolean isReaderClosable = true;

	public CsvRowIterator(File file) throws FileNotFoundException {
		this(new CSVReader(new FileReader(file)));
	}


	public CsvRowIterator(CSVReader reader) {
		this.reader = reader;
	}

	@Override
	protected List<String> prefetch() throws Exception {
	
		String[] cells;
		if((cells = reader.readNext()) == null) {
			return finish();
		}
	
		List<String> result = Arrays.asList(cells);	

		return result;
	}

	@Override
	public void close() {
		if(isReaderClosable) {
			try {
			reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}




class TsvRowIterator
	extends SinglePrefetchIterator<List<String>>
{
	private BufferedReader reader;
	private String separator;
	private boolean isReaderClosable = true;
	
	public TsvRowIterator(File file, String separator) throws FileNotFoundException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(file))), separator);
	}
	
	
	public TsvRowIterator(BufferedReader reader, String separator) {
		this.reader = reader;
		this.separator = separator;
	}

	@Override
	protected List<String> prefetch() throws Exception {
		
		String line;
		if((line = reader.readLine()) == null) {
			return finish();
		}
		
		String[] cells = line.split(separator);
		List<String> result = Arrays.asList(cells);
		

		return result;
	}
	
	@Override
	public void close() {
		if(isReaderClosable) {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}


class XlsRowIterator<T>
	extends SinglePrefetchIterator<List<T>>
{
	private Sheet sheet;
	private int currentRow;
	
	private Transformer<Cell, T> cellTransformer;
	
	private Workbook workbook;
	
	
	public XlsRowIterator(Sheet sheet, int startRow, Transformer<Cell, T> cellTransformer, Workbook workbook)
	{
		this.sheet = sheet;
		this.currentRow = startRow;
		this.cellTransformer = cellTransformer;
		this.workbook = workbook;
	}
	
	
	@Override
	protected List<T> prefetch() throws Exception {
		if(currentRow >= sheet.getRows()) {
			return finish();
		}
		
		Cell[] tmp = sheet.getRow(currentRow++);
		
		List<T> result = new ArrayList<T>();
		for(int i = 0; i < tmp.length; ++i) {
			Cell cell = tmp[i];
			T value = this.cellTransformer != null ? this.cellTransformer.transform(cell) : (T)cell;
			
			result.add(value);
		}
		
		//List<Cell> result = Arrays.asList(tmp);
		
		return result;
	}
	
	@Override
	public void close() {
		super.close();
		
		if(workbook != null) {
			workbook.close();
		}
	}
	
}


public class Main {
	public static void main(String[] args) throws Exception {

		/*
		File configFile = new File(configFileStr);
		if (!configFile.exists()) {
			logger.error("File does not exist: " + configFileStr);

			printHelpAndExit(-1);
		}

		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in);
		} finally {
			in.close();
		}

		
		File srcFile = new File(
				"/home/raven/Documents/OpenDataPortal/Datasets/fp7_ict_project_partners_database_2007_2011.xls");

		convertXlsToCsv(srcFile);
		*/
	}

	/**
	 * Source:
	 * http://www.roseindia.net/answers/viewqa/Development-process/17209-
	 * sir-how-
	 * to-convert-excel-file-to-csv-file-using-java-please-send-me--sample
	 * -code..html
	 * 
	 * @param args
	 */
	public static void convertXlsToCsv(File file) throws Exception {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(new Locale("en", "EN"));
		Workbook w = Workbook.getWorkbook(file, ws);

		
		
		File f = new File("/tmp/new.csv");
		OutputStream os = (OutputStream) new FileOutputStream(f);
		String encoding = "UTF8";
		OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		BufferedWriter bw = new BufferedWriter(osw);

		for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++) {
			Sheet s = w.getSheet(sheet);

			bw.write(s.getName());
			bw.newLine();

			Cell[] row = null;

			for (int i = 0; i < s.getRows(); i++) {
				row = s.getRow(i);

				if (row.length > 0) {
					bw.write(row[0].getContents());
					
					for (int j = 1; j < row.length; j++) {
						bw.write('\t');
						bw.write(row[j].getContents());
					}
				}
				bw.newLine();
			}
		}
		bw.flush();
		bw.close();

	}
}
