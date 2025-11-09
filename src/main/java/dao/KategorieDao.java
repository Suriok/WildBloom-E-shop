package dao;

import jakarta.persistence.NoResultException;
import model.Kategorie;
import org.springframework.stereotype.Repository;

@Repository
public class KategorieDao extends BaseDao<Kategorie> {

    public KategorieDao() { super(Kategorie.class); }

    public Kategorie findByNazevIgnoreCase(String nazev) {
        try {
            return em.createQuery(
                            "SELECT k FROM Kategorie k WHERE LOWER(k.nazev) = LOWER(:nazev)", Kategorie.class)
                    .setParameter("nazev", nazev)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}


