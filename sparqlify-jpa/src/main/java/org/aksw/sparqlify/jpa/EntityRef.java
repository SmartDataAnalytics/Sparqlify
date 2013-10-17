package org.aksw.sparqlify.jpa;


/**
 * A reference to a Jpa entity
 * 
 * TODO Maybe there is an already existing class which already serves the same purpose
 * 
 * @author raven
 *
 */
public class EntityRef {
	private Class<?> entityClass;
	private Object instanceId;
	
	public EntityRef(Class<?> entityClass, Object instanceId) {
		super();
		this.entityClass = entityClass;
		this.instanceId = instanceId;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public Object getInstanceId() {
		return instanceId;
	}
	
	@Override
	public String toString() {
		return "EntityRef [entityClass=" + entityClass + ", instanceId="
				+ instanceId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityClass == null) ? 0 : entityClass.hashCode());
		result = prime * result
				+ ((instanceId == null) ? 0 : instanceId.hashCode());
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
		EntityRef other = (EntityRef) obj;
		if (entityClass == null) {
			if (other.entityClass != null)
				return false;
		} else if (!entityClass.equals(other.entityClass))
			return false;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		return true;
	}
}
