package dao;

import jakarta.persistence.NoResultException;
import model.Uzivatel;
import org.springframework.stereotype.Repository;

@Repository
public class UzivatelDao extends BaseDao<Uzivatel> {

    public UzivatelDao() {
        super(Uzivatel.class);
    }

    public Uzivatel findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM Uzivatel u WHERE u.email = :email", Uzivatel.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByEmail(String email) {
        Long cnt = em.createQuery("SELECT COUNT(u) FROM Uzivatel u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }
}