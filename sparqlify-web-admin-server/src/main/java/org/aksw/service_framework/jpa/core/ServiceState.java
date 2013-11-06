package org.aksw.service_framework.jpa.core;




public class ServiceState
{
	private Object configId;
	private Object executionId;
	private Object executionContextId;
	
	public ServiceState(Object configId, Object executionId, Object executionContextId) {
		this.configId = configId;
		this.executionId = executionId;
		this.executionContextId = executionContextId;
	}

	public Object getConfigId() {
		return configId;
	}
	
	public Object getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Object executionId) {
		this.executionId = executionId;
	}

	public Object getExecutionContextId() {
		return executionContextId;
	}

	public void setExecutionContextId(Object executionContextId) {
		this.executionContextId = executionContextId;
	}

	@Override
	public String toString() {
		return "ServiceJpaWrapper ["
			    + "configId="
				+ configId + ", executionId=" + executionId
				+ ", executionContextId=" + executionContextId + "]";
	}

}

