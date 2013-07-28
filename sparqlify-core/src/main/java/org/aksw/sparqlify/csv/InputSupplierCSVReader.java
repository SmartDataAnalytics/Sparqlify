package org.aksw.sparqlify.csv;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

public class InputSupplierCSVReader
	implements InputSupplier<CSVReader>
{
	private CsvParserConfig config;
	private InputSupplier<? extends Reader> readerSupplier;
	
	public InputSupplierCSVReader(File file, CsvParserConfig config) {
		this(Files.newReaderSupplier(file, Charset.defaultCharset()), config);
	}
	
	public InputSupplierCSVReader(InputSupplier<? extends Reader> readerSupplier, CsvParserConfig config) {
		this.readerSupplier = readerSupplier;
		this.config = config;
	}
	
	@Override
	public CSVReader getInput() throws IOException {
		char fieldSep = config.getFieldSeparator() == null ? CSVParser.DEFAULT_SEPARATOR : config.getFieldSeparator();
		char quoteChar = config.getFieldDelimiter() == null ? CSVParser.DEFAULT_QUOTE_CHARACTER : config.getFieldDelimiter();		
		char escapeChar = config.getEscapeCharacter() == null ? CSVParser.DEFAULT_ESCAPE_CHARACTER : config.getEscapeCharacter();
		
		Reader reader = readerSupplier.getInput();
		CSVReader result = new CSVReader(reader, fieldSep, quoteChar, escapeChar, 0, false);
		return result;
	}
}
