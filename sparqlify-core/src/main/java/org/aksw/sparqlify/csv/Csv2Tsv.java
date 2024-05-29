//package org.aksw.sparqlify.csv;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.base.Joiner;
//
//public class Csv2Tsv {
//	private static final Logger logger = LoggerFactory
//			.getLogger(Csv2Tsv.class);
//
//	public static void test() {
//		String original = "this\\tis\\\\\\ta\\\\test\\nsecond line\\\\\\n and third";
//		//String original = "one\\ntwo";
//		
//		String unescaped = Csv2Tsv.unescapeTsvField(original);
//		String escaped = Csv2Tsv.escapeTsvField(unescaped);
//
//		System.out.println("-----");
//		System.out.println(original);
//		System.out.println("-----");
//		System.out.println(unescaped);
//		System.out.println("-----");
//		System.out.println(escaped);
//		System.out.println("-----");
//
//		
//		if(!original.equals(escaped)) {
//			throw new RuntimeException("Fail");
//		}
//		
//		System.exit(0);
//	}
//	
//	public static void main(String[] args) throws FileNotFoundException {
//
//		//test();
//		
//		
//		if(args.length != 1) {
//			throw new RuntimeException("This tool takes exactly 1 argument, which is the filename of the CSV file to export to TSV.");
//		}
//
//		String filename = args[0];
//		File file = new File(filename);
//		
//		Iterator<List<String>> it = CsvMapperCliMain.getCsvIterator(file, "\t");
//
//		while(it.hasNext()) {
//			List<String> line = it.next();
//			
//			List<String> encoded = new ArrayList<String>();
//			for(String cell : line) {
//				encoded.add(escapeTsvField(cell));
//			}
//			
//			
//			String resultLine = Joiner.on("\t").join(encoded);
//			
//			System.out.println(resultLine);			
//		}
//	}
//	
//	
//
//	/**
//	 * Escapes backslashes, tabs and newlines
//	 * 
//	 * This should cleanly unescape values from bash:
//	 * sed -r 's|([^\\](\\\\)*)(\\n)|\1\n|g' | sed -r 's|([^\\](\\\\)*)(\\t)|\1\t|g' | sed -r 's|\\\\|\\|g'
//	 * 
//	 * @param value
//	 * @return
//	 */
//	public static String escapeTsvField(String value) {
//		if(value == null) {
//			return null;
//		}
//		
//		String result = value.replace("\\", "\\\\");
//		result = result.replace("\t", "\\t");
//		result = result.replace("\n", "\\n");
//		
//		return result;
//	}
//	
//	public static String unescapeTsvField(String value) {
//		if(value == null) {
//			return null;
//		}
//		
//		String result = value.replaceAll("([^\\\\](\\\\\\\\)*)(\\\\n)", "$1\n");
//		result = result.replaceAll("([^\\\\](\\\\\\\\)*)(\\\\t)", "$1\t");
//		result = result.replace("\\\\", "\\");
//		
//		return result;
//	}
//}
