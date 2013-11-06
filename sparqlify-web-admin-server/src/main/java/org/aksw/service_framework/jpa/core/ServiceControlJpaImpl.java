package org.aksw.service_framework.jpa.core;



public class ServiceControlJpaImpl<C, E, S>
	implements ServiceControl<S>
{
	private ServiceRepositoryJpaImpl<C, E, S> serviceRepo;
	private Object configId;


	public ServiceControlJpaImpl(ServiceRepositoryJpaImpl<C, E, S> serviceRepo, Object configId) {
		this.serviceRepo = serviceRepo;
		this.configId = configId;
	}
	
	public ServiceRepositoryJpaImpl<C, E, S> getServiceRepo() {
		return serviceRepo;
	}
	
	public Object getConfigId() {
		return configId;
	}
	
	@Override
	public void start() {
		serviceRepo.startByConfigId(configId);
	}

	@Override
	public void stop() {
		serviceRepo.stopByConfigId(configId);
	}

	@Override
	public S getService() {
		ServiceProvider<S> provider = serviceRepo.getServiceProviderByConfigId(configId);
		S result = provider.getService();
		
		return result;
	}	
}
