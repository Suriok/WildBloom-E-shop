package service;

import dao.KosikDao;
import dao.PolozkaKosikuDao;
import dao.ProduktDao;
import dao.ZakaznikDao;
import model.Kosik;
import model.PolozkaKosiku;
import model.Produkt;
import model.Zakaznik;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

@Service
public class CartService {

    private final ZakaznikDao zakaznikDao;
    private final KosikDao kosikDao;
    private final ProduktDao produktDao;
    private final PolozkaKosikuDao polozkaKosikuDao;

    public CartService(ZakaznikDao zakaznikDao, KosikDao kosikDao, ProduktDao produktDao, PolozkaKosikuDao polozkaKosikuDao) {
        this.zakaznikDao = zakaznikDao;
        this.kosikDao = kosikDao;
        this.produktDao = produktDao;
        this.polozkaKosikuDao = polozkaKosikuDao;
    }

    @Transactional
    public Kosik addItem(Long zakaznikId, Long produktId, int mnozstvi) {
        if (mnozstvi <= 0){
            throw new IllegalArgumentException("Množství musí být > 0");
        }
        final Zakaznik z = ensureZakaznik(zakaznikId);
        Kosik k = kosikDao.findByZakaznikWithItems(z.getId());
        if (k == null){
            k = ensureKosik(z);
        }

        final Produkt p = ensureProdukt(produktId);

        PolozkaKosiku pk = polozkaKosikuDao.findByKosikAndProdukt(k, p);
        if (pk == null) {
            pk = new PolozkaKosiku();
            pk.setKosik(k);
            pk.setProdukt(p);
            pk.setMnozstvi(mnozstvi);
            polozkaKosikuDao.persist(pk);
            k.getPolozky().add(pk);
        } else {
            pk.setMnozstvi(pk.getMnozstvi() + mnozstvi);
            polozkaKosikuDao.update(pk);
        }
        recalculateCartTotal(k);
        kosikDao.update(k);
        return k;
    }

    @Transactional
    public Kosik updateItemQuantity(Long zakaznikId, Long produktId, int newQty) {
        if (newQty < 0){
            throw new IllegalArgumentException("Množství musí být ≥ 0");
        }
        final Zakaznik z = ensureZakaznik(zakaznikId);
        final Kosik k = ensureKosikFor(z);
        final Produkt p = ensureProdukt(produktId);

        PolozkaKosiku pk = polozkaKosikuDao.findByKosikAndProdukt(k, p);
        if (pk == null){
            throw new NoSuchElementException("Položka košíku nenalezena");
        }

        if (newQty == 0) {
            k.getPolozky().remove(pk);
            polozkaKosikuDao.remove(pk);
        } else {
            pk.setMnozstvi(newQty);
            polozkaKosikuDao.update(pk);
        }
        recalculateCartTotal(k);
        kosikDao.update(k);
        return k;
    }

    @Transactional
    public Kosik removeItem(Long zakaznikId, Long produktId) {
        final Zakaznik z = ensureZakaznik(zakaznikId);
        final Kosik k = ensureKosikFor(z);
        final Produkt p = ensureProdukt(produktId);

        PolozkaKosiku pk = polozkaKosikuDao.findByKosikAndProdukt(k, p);
        if (pk != null) {
            k.getPolozky().remove(pk);
            polozkaKosikuDao.remove(pk);
            recalculateCartTotal(k);
            kosikDao.update(k);
        }
        return k;
    }

    // helpers
    private Zakaznik ensureZakaznik(Long id) {
        Zakaznik z = zakaznikDao.find(requireNonNull(id));
        if (z == null){
            throw new NoSuchElementException("Zákazník nenalezen");
        }
        return z;
    }

    private Kosik ensureKosik(Zakaznik z) {
        Kosik k = z.getKosik();
        if (k == null) {
            k = new Kosik();
            k.setZakaznik(z);
            kosikDao.persist(k);
            z.setKosik(k);
            zakaznikDao.update(z);
        }
        return k;
    }

    private Kosik ensureKosikFor(Zakaznik z) {
        Kosik k = kosikDao.findByZakaznikWithItems(z.getId());
        if (k == null){
            k = ensureKosik(z);
        }
        return k;
    }

    private Produkt ensureProdukt(Long produktId) {
        Produkt p = produktDao.find(requireNonNull(produktId));
        if (p == null){
            throw new NoSuchElementException("Produkt nenalezen");
        }
        return p;
    }

    private void recalculateCartTotal(Kosik k) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PolozkaKosiku it : k.getPolozky()) {
            BigDecimal price = it.getProdukt().getCena();
            if (price == null){
                price = BigDecimal.ZERO;
            }
            sum = sum.add(price.multiply(BigDecimal.valueOf(it.getMnozstvi())));
        }
        k.setCelkovaSuma(sum.setScale(2, RoundingMode.HALF_UP));
    }
}
