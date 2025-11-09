package dao;

import jakarta.persistence.NoResultException;
import model.Kosik;
import model.Zakaznik;
import org.springframework.stereotype.Repository;

@Repository
public class KosikDao extends BaseDao<Kosik> {

    public KosikDao() { super(Kosik.class); }

    public Kosik findByZakaznik(Zakaznik zakaznik) {
        try {
            return em.createQuery("SELECT k FROM Kosik k WHERE k.zakaznik = :z", Kosik.class)
                    .setParameter("z", zakaznik)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Kosik findByZakaznikWithItems(Long zakaznikId) {
        try {
            return em.createQuery("""
                    SELECT DISTINCT k
                    FROM Kosik k
                    LEFT JOIN FETCH k.polozky pk
                    LEFT JOIN FETCH pk.produkt p
                    WHERE k.zakaznik.id = :zakaznikId
                    """, Kosik.class)
                    .setParameter("zakaznikId", zakaznikId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
