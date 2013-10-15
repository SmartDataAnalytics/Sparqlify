package org.aksw.service_framework.core;

public abstract class ServiceExecutionBase<T>
	implements ServiceExecution<T>
{
	private String name;
	
	public ServiceExecutionBase(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}