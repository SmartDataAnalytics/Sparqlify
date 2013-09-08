package org.aksw.sparqlify.core.cast;

public class ParamDistance
{	
	private Integer distance;
	private boolean usesCoercion;

	public ParamDistance(Integer distance) {
		this(distance, false);
	}
	
	public ParamDistance(Integer distance, boolean usesCoercion) {
		super();
		this.distance = distance;
		this.usesCoercion = usesCoercion;
	}
	
	public Integer getDistance() {
		return distance;
	}

	public boolean isUsesCoercion() {
		return usesCoercion;
	}

	@Override
	public String toString() {
		return "" + distance +  (usesCoercion ? "(with coercion)" : "");
	}
	
	
	public int compareTo(ParamDistance that) {

		int compareCoercion = (this.usesCoercion ? 1 : -1) - (that.usesCoercion ? 1 : -1);
		if(compareCoercion != 0) {
			return compareCoercion;
		}
		
		return that.distance - distance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + (usesCoercion ? 1231 : 1237);
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
		ParamDistance other = (ParamDistance) obj;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		if (usesCoercion != other.usesCoercion)
			return false;
		return true;
	}
}