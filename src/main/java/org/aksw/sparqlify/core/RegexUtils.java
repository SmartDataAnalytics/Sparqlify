package org.aksw.sparqlify.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RegexUtils
{
	public static final Set<Character> specialChars = new HashSet<Character>(Arrays.asList('.', '\\', '?', '*', '+', '&', ':', '(', ')', '[', ']', '{', '}', '^', '$'));

	public static boolean isSpecialChar(char c) {
		return specialChars.contains(c);
	}
	
	public static String escape(String str) {
		return genericEscape(str, specialChars, '\\');
	}
	
	public static String genericEscape(String str, Set<Character> specialChars, Character escapeChar) {
		final StringBuilder result = new StringBuilder();

		for(int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			
			if(specialChars.contains(c)) {
				result.append(escapeChar);
			}

			result.append(c);
		}
		
		return result.toString();
	}	
}