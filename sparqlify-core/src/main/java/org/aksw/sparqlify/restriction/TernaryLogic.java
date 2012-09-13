package org.aksw.sparqlify.restriction;

public class TernaryLogic {
	// a/b
	//   t n f
	// t t n f
	// n n n f
	// f f f f
	public static Boolean and(Boolean a, Boolean b) {
		if(a == Boolean.TRUE && b == Boolean.TRUE) {
			return Boolean.TRUE;
		} else if(a == Boolean.FALSE || b == Boolean.FALSE) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}
	
}
