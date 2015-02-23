package org.aksw.sparqlify.admin.web.api;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class CollectionJpa<T> {
    private EntityManagerFactory emf;
    private Class<T> clazz;

    public CollectionJpa(Class<T> clazz, EntityManagerFactory emf) {
        this.emf = emf;
        this.clazz = clazz;
    }

    public T get(Object id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        T result = em.find(clazz, id);

        em.getTransaction().commit();
        em.close();

        return result;
    }
}