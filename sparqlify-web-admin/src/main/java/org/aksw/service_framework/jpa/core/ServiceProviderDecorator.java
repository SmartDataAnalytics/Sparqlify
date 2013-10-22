package org.aksw.service_framework.jpa.core;

public abstract class ServiceProviderDecorator<S>
	implements ServiceProvider<S>
{
	private ServiceProvider<S> decoratee;

	public ServiceProviderDecorator(ServiceProvider<S> decoratee) {
		this.decoratee = decoratee;
	}
	
	@Override
	public String getName() {
		String result = decoratee.getName();
		return result;
	}

	@Override
	public S getService() {
		S result = decoratee.getService();
		return result;
	}

	@Override
	public void close() {
		decoratee.close();
	}
}
