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
        try {
            return em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type)
                    .getResultList();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public T update(T entity) {
        Objects.requireNonNull(entity);
        try{
            return em.merge(entity);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(T entity) {
        Objects.requireNonNull(entity);
        try{
            em.remove(em.contains(entity) ? entity : em.merge(entity));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }


    }
}

