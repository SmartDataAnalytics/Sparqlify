package org.aksw.sparqlify.database;

public class StartsWithConstraint
	implements Constraint
{
	private String prefix;
	private boolean inclusive;
	
	public StartsWithConstraint(String prefix) {
		this(prefix, true);
	}
	
	public StartsWithConstraint(String prefix, boolean inclusive) {
		this.prefix = prefix;
		this.inclusive = inclusive;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public boolean isInclusive() {
		return inclusive;
	}

	@Override
	public boolean isSatisfiedBy(Object value) {
		return value.toString().startsWith(prefix) && !(!inclusive && value.toString().equals(prefix));
	}

	@Override
	public String toString() {
		return "startsWith " + prefix + (inclusive ? "" : " (non-inclusive)");
	}
}
