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


	@Override
	public String toString() {
		return "MethodDistance [" + returnTypeDistance + "; " 
				+ argTypeDistances + "]";
	}
	
	
	public Integer compare(MethodDistance that) {
		Integer result = null;
		
		if(this.argTypeDistances.size() == that.argTypeDistances.size()) {
			
			result = 0;
			for(int i = 0; i < this.argTypeDistances.size(); ++i) {
				ParamDistance a = this.argTypeDistances.get(i);
				ParamDistance b = that.argTypeDistances.get(i);
				
				int d = a.compareTo(b);
				if(result == -d) {
					result = null;
					break;
				}
				
				result = d;
			}
		} 
		
		return result;
	}
}

