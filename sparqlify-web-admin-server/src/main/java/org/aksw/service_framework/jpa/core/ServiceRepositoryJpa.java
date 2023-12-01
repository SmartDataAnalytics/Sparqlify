package org.aksw.service_framework.jpa.core;

import jakarta.persistence.EntityManagerFactory;

import org.aksw.service_framework.core.ServiceRepository;

public interface ServiceRepositoryJpa<S>
	extends ServiceRepository<S>
{
	EntityManagerFactory getEntityManagerFactory();
}
