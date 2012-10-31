package org.aksw.sparqlify.restriction.experiment;

public enum Satisfiability {
	UNKNOWN(null),
	FALSE(false),
	TRUE(true);
	
	private Boolean value;
	
	private Satisfiability(Boolean value) {
		this.value = value;
	}
	
	public Boolean asBoolean() {
		return value;
	}
}
