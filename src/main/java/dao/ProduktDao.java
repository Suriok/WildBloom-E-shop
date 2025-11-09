package dao;
import model.Kategorie;
import model.Produkt;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProduktDao extends BaseDao<Produkt> {

    public ProduktDao() { super(Produkt.class); }

    public List<Produkt> findByKategorie(Kategorie kategorie) {
        return em.createQuery("SELECT p FROM Produkt p WHERE p.kategorie = :k", Produkt.class)
                .setParameter("k", kategorie)
                .getResultList();
    }
}