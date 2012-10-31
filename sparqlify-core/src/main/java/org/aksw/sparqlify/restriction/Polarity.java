package org.aksw.sparqlify.restriction;

public enum Polarity {
	POSITIVE(true),
	NEGATIVE(false);
	
	private boolean polarity;

	Polarity(boolean polarity) {
		this.polarity = polarity;
	}
	
	public boolean isPositive() {
		return polarity;
	}
}