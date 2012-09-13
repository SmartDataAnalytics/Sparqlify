package org.aksw.sparqlify.database;

/**
 * Constraint for a variable.
 * 
 * FIXME Might make sense to make the variable type a generic,
 * so we can use String or Var as we see fit.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class VariableConstraint
{
	private String variableName;
	private Constraint constraint;
	
	public VariableConstraint(String variableName, Constraint constraint) {
		this.variableName = variableName;
		this.constraint = constraint;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public Constraint getConstraint() {
		return constraint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constraint == null) ? 0 : constraint.hashCode());
		result = prime * result
				+ ((variableName == null) ? 0 : variableName.hashCode());
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
		VariableConstraint other = (VariableConstraint) obj;
		if (constraint == null) {
			if (other.constraint != null)
				return false;
		} else if (!constraint.equals(other.constraint))
			return false;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return variableName + " -> " + constraint;
	}
}
