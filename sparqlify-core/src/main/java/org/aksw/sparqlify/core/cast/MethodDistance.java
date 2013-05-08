package org.aksw.sparqlify.core.cast;

import java.util.Collections;
import java.util.List;

public class MethodDistance {
	private ParamDistance returnTypeDistance;
	private List<ParamDistance> argTypeDistances;

	public MethodDistance(Integer returnTypeDistance,
			Integer argTypeDistance) {
		this(new ParamDistance(returnTypeDistance), new ParamDistance(argTypeDistance));
	}

	
	public MethodDistance(ParamDistance returnTypeDistance,
			ParamDistance argTypeDistance) {
		this(returnTypeDistance, Collections.singletonList(argTypeDistance));
	}
	
	public MethodDistance(ParamDistance returnTypeDistance,
			List<ParamDistance> argTypeDistances) {
		super();
		this.returnTypeDistance = returnTypeDistance;
		this.argTypeDistances = argTypeDistances;
	}

	public ParamDistance getReturnTypeDistance() {
		return returnTypeDistance;
	}

	public List<ParamDistance> getArgTypeDistances() {
		return argTypeDistances;
	}

}