package org.aksw.sparqlify.core.datatypes;

import java.util.List;

class InvocableComposite
	implements Invocable
{
	private Invocable main;
	private Invocable[] argTransforms;
	
	public InvocableComposite(Invocable main, Invocable[] argTransforms) {
		this.main = main;
		this.argTransforms = argTransforms;
	}
	
	@Override
	public Object invoke(Object... args) {
		Object[] transformedArgs;
		
		if(argTransforms == null) {
			transformedArgs = args;
		} else {
			transformedArgs = new Object[args.length];
			
			for(int i = 0; i < argTransforms.length; ++i) {
				Object arg = args[i];
				Invocable transformer = argTransforms[i];
				
				Object transformedArg;
				if(transformer == null) {
					transformedArg = arg;
				} else {
					transformedArg = transformer.invoke(arg);
				}
				
				transformedArgs[i] = transformedArg;
			}
		}
		
		Object result = main.invoke(transformedArgs);
		
		return result;
	}
	
}

public class SqlMethodCandidate {
	private XMethod method;
	private List<XMethod> argCoercions;
	
	public SqlMethodCandidate(XMethod method, List<XMethod> argCoercions) {
		this.method = method;
		this.argCoercions = argCoercions;
	}

	public XMethod getMethod() {
		return method;
	}

	public List<XMethod> getArgCoercions() {
		return argCoercions;
	}

	public Invocable getInvocable() {
		
		Invocable result;
		Invocable[] invocables = null;
		if(argCoercions != null) {
			invocables = new Invocable[argCoercions.size()];
			for(int i = 0; i < argCoercions.size(); ++i) {
				XMethod argCoercion = argCoercions.get(i);
				
				if(argCoercion != null) {
					invocables[i] = argCoercion.getInvocable();
				}
			}
		}

		result = new InvocableComposite(method.getInvocable(), invocables);
		
		return result;		
	}
	
	
	@Override
	public String toString() {
		return "MethodCandidate [method=" + method + ", argCoercions="
				+ argCoercions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argCoercions == null) ? 0 : argCoercions.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		SqlMethodCandidate other = (SqlMethodCandidate) obj;
		if (argCoercions == null) {
			if (other.argCoercions != null)
				return false;
		} else if (!argCoercions.equals(other.argCoercions))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}
}
