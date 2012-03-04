package org.aksw.sparqlify.database;

public class EqualsConstraint
	implements Constraint
{
	private Object value;
	
	public EqualsConstraint(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isSatisfiedBy(Object value) {
		return this.value.equals(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		EqualsConstraint other = (EqualsConstraint) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "equalsConstraint(" + value + ")";
	}
	
	
}
