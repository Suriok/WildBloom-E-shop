package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "objednavka")
@NamedQuery(
        name = "Objednavka.findByStatus",
        query = "SELECT o FROM Objednavka o WHERE o.stav = :stav ORDER BY o.datum ASC"
)
public class Objednavka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "objednavka_id")
    private Long objednavkaId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StavObjednavky stav;

    @Column(precision = 12, scale = 2)
    private BigDecimal celkovaCena;

    @Column(precision = 12, scale = 2)
    private BigDecimal dph;

    @Column(precision = 12, scale = 2)
    private BigDecimal sleva;

    @Column(precision = 12, scale = 2)
    private BigDecimal doprava;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zakaznik_id", nullable = false)
    private Zakaznik zakaznik;

    @OneToMany(mappedBy = "objednavka", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolozkaObjednavky> polozky = new ArrayList<>();

    public Objednavka() {}

    @PrePersist
    void prePersist() {
        if (datum == null) datum = new Date();
        if (stav == null) stav = StavObjednavky.CEKA_NA_POTVRZENI;
        if (celkovaCena == null) celkovaCena = BigDecimal.ZERO;
    }

    public Long getObjednavkaId() { return objednavkaId; }
    public Date getDatum() { return datum; }
    public StavObjednavky getStav() { return stav; }
    public void setStav(StavObjednavky stav) { this.stav = stav; }
    public BigDecimal getCelkovaCena() { return celkovaCena; }
    public void setCelkovaCena(BigDecimal celkovaCena) { this.celkovaCena = celkovaCena; }
    public BigDecimal getDph() { return dph; }
    public void setDph(BigDecimal dph) { this.dph = dph; }
    public BigDecimal getSleva() { return sleva; }
    public void setSleva(BigDecimal sleva) { this.sleva = sleva; }
    public BigDecimal getDoprava() { return doprava; }
    public void setDoprava(BigDecimal doprava) { this.doprava = doprava; }
    public Zakaznik getZakaznik() { return zakaznik; }
    public void setZakaznik(Zakaznik zakaznik) { this.zakaznik = zakaznik; }
    public List<PolozkaObjednavky> getPolozky() { return polozky; }
}