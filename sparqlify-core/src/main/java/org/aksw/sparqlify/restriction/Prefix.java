package org.aksw.sparqlify.restriction;

public class Prefix {
	private String prefix;
	
	// Whether the prefix is actually the constant itself (i.e. a single element set)
	private boolean isConstant;

	public Prefix(String prefix) {
		this(prefix, false);
	}

	public Prefix(String prefix, boolean isConstant) {
		this.prefix = prefix;
		this.isConstant = isConstant;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public boolean isConstant() {
		return isConstant;
	}
	
	public boolean isPrefixOf(Prefix that) {
		boolean result;
		if(this.isConstant) {
			result = that.isConstant && that.prefix.equals(this.prefix);
		} else {
			result = that.prefix.startsWith(this.prefix);
		}
		
		return result;
	}
}