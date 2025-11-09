package dao;

import jakarta.persistence.NoResultException;
import model.Kosik;
import model.PolozkaKosiku;
import model.Produkt;
import org.springframework.stereotype.Repository;

@Repository
public class PolozkaKosikuDao extends BaseDao<PolozkaKosiku> {

    public PolozkaKosikuDao() { super(PolozkaKosiku.class); }

    public PolozkaKosiku findByKosikAndProdukt(Kosik kosik, Produkt produkt) {
        try {
            return em.createQuery("""
                    SELECT pk FROM PolozkaKosiku pk
                    WHERE pk.kosik = :k AND pk.produkt = :p
                    """, PolozkaKosiku.class)
                    .setParameter("k", kosik)
                    .setParameter("p", produkt)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
