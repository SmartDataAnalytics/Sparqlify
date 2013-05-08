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
}