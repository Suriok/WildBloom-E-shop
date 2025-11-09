package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Objects;

public abstract class BaseDao<T> {

    @PersistenceContext
    protected EntityManager em;

    protected final Class<T> type;

    protected BaseDao(Class<T> type) {
        this.type = type;
    }

    public T find(Long id) {
        Objects.requireNonNull(id);
        return em.find(type, id);
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type)
                .getResultList();
    }

    public void persist(T entity) {
        Objects.requireNonNull(entity);
        em.persist(entity);
    }

    public T update(T entity) {
        Objects.requireNonNull(entity);
        return em.merge(entity);
    }

    public void remove(T entity) {
        Objects.requireNonNull(entity);
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}

