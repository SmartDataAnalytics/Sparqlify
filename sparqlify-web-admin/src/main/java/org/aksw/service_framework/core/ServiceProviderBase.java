package org.aksw.service_framework.core;

import org.aksw.service_framework.jpa.core.ServiceProvider;

public abstract class ServiceProviderBase<T>
	implements ServiceProvider<T>
{
	private String name;
	
	public ServiceProviderBase(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}