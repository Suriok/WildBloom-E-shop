package service;

import dao.*;
import model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Service
public class OrderService {

    private final ZakaznikDao zakaznikDao;
    private final KosikDao kosikDao;
    private final ProduktDao produktDao;
    private final ObjednavkaDao objednavkaDao;
    private final PolozkaObjednavkyDao polozkaObjednavkyDao;

    private static final BigDecimal DPH_RATE = new BigDecimal("0.21"); // 21%
    private static final BigDecimal DOPRAVA = new BigDecimal("50.00");

    private static final Map<StavObjednavky, Set<StavObjednavky>> ALLOWED = new EnumMap<>(StavObjednavky.class);
    static {
        ALLOWED.put(StavObjednavky.CEKA_NA_POTVRZENI, Set.of(StavObjednavky.POTVRZENO, StavObjednavky.ZRUSENO));
        ALLOWED.put(StavObjednavky.POTVRZENO, Set.of(StavObjednavky.V_DOPRAVE, StavObjednavky.ZRUSENO));
        ALLOWED.put(StavObjednavky.V_DOPRAVE, Set.of(StavObjednavky.DORUCENO));
        ALLOWED.put(StavObjednavky.DORUCENO, Set.of());
        ALLOWED.put(StavObjednavky.ZRUSENO, Set.of());
    }

    public OrderService(ZakaznikDao zakaznikDao, KosikDao kosikDao, ProduktDao produktDao, ObjednavkaDao objednavkaDao, PolozkaObjednavkyDao polozkaObjednavkyDao) {
        this.zakaznikDao = zakaznikDao;
        this.kosikDao = kosikDao;
        this.produktDao = produktDao;
        this.objednavkaDao = objednavkaDao;
        this.polozkaObjednavkyDao = polozkaObjednavkyDao;
    }

    @Transactional
    public Objednavka createOrderFromCart(Long zakaznikId) {
        final Zakaznik z = ensureZakaznik(zakaznikId);
        final Kosik k = ensureCartWithItems(z.getId());
        if (k.getPolozky().isEmpty()){
            throw new IllegalStateException("Košík je prázdný");
        }

        for (PolozkaKosiku it : k.getPolozky()) {
            Produkt p = it.getProdukt();
            if (p.getSkladem() < it.getMnozstvi()) {
                throw new IllegalStateException("Nedostatek zboží: " + p.getNazev());
            }
        }

        Objednavka o = new Objednavka();
        o.setZakaznik(z);
        objednavkaDao.persist(o);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PolozkaKosiku it : k.getPolozky()) {
            Produkt p = it.getProdukt();

            PolozkaObjednavky po = new PolozkaObjednavky();
            po.setObjednavka(o);
            po.setProdukt(p);
            po.setMnozstvi(it.getMnozstvi());
            po.setCenaSnapshot(p.getCena());
            polozkaObjednavkyDao.persist(po);
            o.getPolozky().add(po);

            p.setSkladem(p.getSkladem() - it.getMnozstvi());
            produktDao.update(p);

            subtotal = subtotal.add(p.getCena().multiply(BigDecimal.valueOf(it.getMnozstvi())));
        }

        BigDecimal dph = subtotal.multiply(DPH_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(dph).add(DOPRAVA).setScale(2, RoundingMode.HALF_UP);

        o.setDph(dph);
        o.setDoprava(DOPRAVA);
        o.setCelkovaCena(total);
        objednavkaDao.update(o);

        k.getPolozky().clear();
        k.setCelkovaSuma(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        kosikDao.update(k);

        return o;
    }

    @Transactional
    public void cancelOrder(Long zakaznikId, Long objednavkaId) {
        final Zakaznik z = ensureZakaznik(zakaznikId);
        Objednavka o = objednavkaDao.find(requireNonNull(objednavkaId));
        if (o == null){
            throw new NoSuchElementException("Objednávka nenalezena");
        }
        if (!o.getZakaznik().getId().equals(z.getId())) {
            throw new IllegalStateException("Objednávka nepatří uživateli");
        }
        if (!(o.getStav() == StavObjednavky.CEKA_NA_POTVRZENI || o.getStav() == StavObjednavky.POTVRZENO)) {
            throw new IllegalStateException("Zrušení není povoleno pro stav: " + o.getStav());
        }

        Objednavka withItems = objednavkaDao.findByIdWithItems(objednavkaId);
        if (withItems == null) withItems = o;

        for (PolozkaObjednavky po : withItems.getPolozky()) {
            Produkt p = po.getProdukt();
            p.setSkladem(p.getSkladem() + po.getMnozstvi());
            produktDao.update(p);
        }

        o.setStav(StavObjednavky.ZRUSENO);
        objednavkaDao.update(o);
    }

    @Transactional
    public Objednavka changeStatus(Long objednavkaId, StavObjednavky novyStav) {
        Objednavka o = objednavkaDao.find(requireNonNull(objednavkaId));
        if (o == null){
            throw new NoSuchElementException("Objednávka nenalezena");
        }

        final StavObjednavky from = o.getStav();
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(novyStav)) {
            throw new IllegalStateException("Neplatný přechod: " + from + " -> " + novyStav);
        }
        o.setStav(novyStav);
        return objednavkaDao.update(o);
    }

    // helpers
    private Zakaznik ensureZakaznik(Long id) {
        Zakaznik z = zakaznikDao.find(requireNonNull(id));
        if (z == null){
            throw new NoSuchElementException("Zákazník nenalezen");
        }
        return z;
    }

    private Kosik ensureCartWithItems(Long zakaznikId) {
        Kosik k = kosikDao.findByZakaznikWithItems(requireNonNull(zakaznikId));
        if (k == null){
            throw new NoSuchElementException("Košík nenalezen");
        }
        return k;
    }
}
