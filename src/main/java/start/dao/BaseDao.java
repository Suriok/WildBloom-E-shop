package start.dao;

import start.dao.exception.DaoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.PersistenceException;

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
        try {
            return em.find(type, id);
        } catch (PersistenceException e) {
            throw new DaoException("Chyba při hledání entity " + type.getSimpleName() + " s id " + id, e);
        }
    }

    public List<T> findAll() {
        try {
            return em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new DaoException("Chyba při načítání seznamu entit " + type.getSimpleName(), e);
        }
    }

    @Transactional
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (PersistenceException e) {
            throw new DaoException("Nepodařilo se uložit entitu " + entity, e);
        }
    }

    @Transactional
    public T update(T entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity);
        } catch (PersistenceException e) {
            throw new DaoException("Nepodařilo se aktualizovat entitu " + entity, e);
        }
    }

    @Transactional
    public void remove(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
        } catch (PersistenceException e) {
            throw new DaoException("Nepodařilo se smazat entitu " + entity, e);
        }
    }
}