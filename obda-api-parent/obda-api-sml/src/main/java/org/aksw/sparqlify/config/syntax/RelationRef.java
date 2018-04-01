package org.aksw.sparqlify.config.syntax;

public class RelationRef
	implements Relation
{

	private String relationName;
	
	public RelationRef(String relationName) {
		super();
		this.relationName = relationName;
	}

	public String getRelationName()
	{
		return relationName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((relationName == null) ? 0 : relationName.hashCode());
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
		RelationRef other = (RelationRef) obj;
		if (relationName == null) {
			if (other.relationName != null)
				return false;
		} else if (!relationName.equals(other.relationName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return relationName;
	}

	
}
