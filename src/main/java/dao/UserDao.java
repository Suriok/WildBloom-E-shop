package dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import dao.exception.DaoException;
import model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }catch (PersistenceException e) {
            throw new DaoException("Error finding user by email: " + email, e);
        }
    }

    public boolean existsByEmail(String email) {
        try {
            Long cnt = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return cnt != null && cnt > 0;
        }catch (PersistenceException e) {
            throw new DaoException("Error checking if user exists by email: " + email, e);
        }
    }
}
