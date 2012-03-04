package org.aksw.sparqlify.database;

public class IsPrefixOfConstraint
	implements Constraint
{
	private String value;
	private boolean inclusive;
	
	public IsPrefixOfConstraint(String prefix) {
		this(prefix, true);
	}
	
	public IsPrefixOfConstraint(String prefix, boolean inclusive) {
		this.value = prefix;
		this.inclusive = inclusive;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isInclusive() {
		return inclusive;
	}

	@Override
	public boolean isSatisfiedBy(Object prefix) {
		if(prefix == null) {
			return false;
		}
		
		String p = prefix.toString();
		
		return value.startsWith(p) && !(!inclusive && value.equals(p));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (inclusive ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IsPrefixOfConstraint other = (IsPrefixOfConstraint) obj;
		if (inclusive != other.inclusive)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IsPrefixOfConstraint(" + value + ", " + inclusive + ")";
	}	

	
}
