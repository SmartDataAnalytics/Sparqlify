package org.aksw.sparqlify.core.test;

import org.h2.tools.Csv;

public class CsvParserConfig {
	private Character fieldDelimiter = null;
	private Character fieldSeparator = null;
	private Character escapeCharacter = null;
	
	public CsvParserConfig() {
		
	}

	public Character getFieldDelimiter() {
		return fieldDelimiter;
	}

	public void setFieldDelimiter(Character fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	public Character getFieldSeparator() {
		return fieldSeparator;
	}

	public void setFieldSeparator(Character fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	public Character getEscapeCharacter() {
		return escapeCharacter;
	}

	public void setEscapeCharacter(Character escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	/**
	 * Configure a Csv object based on the settings
	 * 
	 * 
	 * @param csv
	 * @return
	 */
	public Csv configure(Csv csv) {
		//csv.setLineSeparator("\n");
		if(fieldDelimiter != null) {
			csv.setFieldDelimiter(fieldDelimiter);
		}

		if(fieldSeparator != null) {
			csv.setFieldSeparatorRead(fieldSeparator);
		}

		if(escapeCharacter != null) {
			csv.setEscapeCharacter(escapeCharacter);
		}
		
		return csv;
	}
}