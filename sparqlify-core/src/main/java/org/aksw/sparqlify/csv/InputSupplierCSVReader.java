package org.aksw.sparqlify.csv;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import com.google.common.io.Files;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class InputSupplierCSVReader
	implements Supplier<CSVReader>
{
	private CsvParserConfig config;
	private Supplier<? extends Reader> readerSupplier;
	
	public InputSupplierCSVReader(File file, Charset charset, CsvParserConfig config) {
		this(() -> {
			Reader r;
			try {
				r = Files.asCharSource(file, charset).openStream();
			} catch (IOException e) {
				throw new RuntimeException();
			}
			return r;
		}, config);
	}
	
	public InputSupplierCSVReader(Supplier<? extends Reader> readerSupplier, CsvParserConfig config) {
		this.readerSupplier = readerSupplier;
		this.config = config;
	}
	
	@Override
	public CSVReader get() { //throws IOException {
		char fieldSep = config.getFieldSeparator() == null ? CSVParser.DEFAULT_SEPARATOR : config.getFieldSeparator();
		char quoteChar = config.getFieldDelimiter() == null ? CSVParser.DEFAULT_QUOTE_CHARACTER : config.getFieldDelimiter();		
		char escapeChar = config.getEscapeCharacter() == null ? CSVParser.DEFAULT_ESCAPE_CHARACTER : config.getEscapeCharacter();
		
		Reader reader = readerSupplier.get();
		CSVReader result = new CSVReader(reader, fieldSep, quoteChar, escapeChar, 0, false);
		return result;
	}
}
