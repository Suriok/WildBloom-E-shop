package dao;

import jakarta.persistence.NoResultException;
import model.Objednavka;
import model.PolozkaObjednavky;
import model.Produkt;
import org.springframework.stereotype.Repository;

@Repository
public class PolozkaObjednavkyDao extends BaseDao<PolozkaObjednavky> {

    public PolozkaObjednavkyDao() { super(PolozkaObjednavky.class); }

    public PolozkaObjednavky findByObjednavkaAndProdukt(Objednavka objednavka, Produkt produkt) {
        try {
            return em.createQuery("""
                    SELECT po FROM PolozkaObjednavky po
                    WHERE po.objednavka = :o AND po.produkt = :p
                    """, PolozkaObjednavky.class)
                    .setParameter("o", objednavka)
                    .setParameter("p", produkt)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean existsByObjednavkaAndProdukt(Objednavka objednavka, Produkt produkt) {
        Long cnt = em.createQuery("""
                SELECT COUNT(po) FROM PolozkaObjednavky po
                WHERE po.objednavka = :o AND po.produkt = :p
                """, Long.class)
                .setParameter("o", objednavka)
                .setParameter("p", produkt)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }
}
