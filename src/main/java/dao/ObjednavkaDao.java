package dao;

import jakarta.persistence.NoResultException;
import model.Objednavka;
import model.StavObjednavky;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ObjednavkaDao extends BaseDao<Objednavka> {

    public ObjednavkaDao() { super(Objednavka.class); }

    public List<Objednavka> findByStavOrderByDatumAsc(StavObjednavky stav) {
        return em.createQuery("""
                SELECT o FROM Objednavka o
                WHERE o.stav = :stav
                ORDER BY o.datum ASC
                """, Objednavka.class)
                .setParameter("stav", stav)
                .getResultList();
    }

    public List<Objednavka> findByZakaznikId(Long zakaznikId) {
        return em.createQuery("""
                SELECT o FROM Objednavka o
                WHERE o.zakaznik.id = :zakId
                ORDER BY o.datum DESC
                """, Objednavka.class)
                .setParameter("zakId", zakaznikId)
                .getResultList();
    }

    public Objednavka findByIdWithItems(Long objednavkaId) {
        try {
            return em.createQuery("""
                    SELECT DISTINCT o
                    FROM Objednavka o
                    LEFT JOIN FETCH o.polozky po
                    LEFT JOIN FETCH po.produkt p
                    WHERE o.objednavkaId = :id
                    """, Objednavka.class)
                    .setParameter("id", objednavkaId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}




