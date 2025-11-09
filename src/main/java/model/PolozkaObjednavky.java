package model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "polozka_objednavky",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_objednavka_produkt",
                columnNames = {"objednavka_id", "produkt_id"}
        )
)
public class PolozkaObjednavky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "objednavka_id", nullable = false)
    private Objednavka objednavka;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produkt_id", nullable = false)
    private Produkt produkt;

    @Column(nullable = false)
    private int mnozstvi;

    @Column(name = "cena_snapshot", precision = 12, scale = 2, nullable = false)
    private BigDecimal cenaSnapshot;

    public PolozkaObjednavky() {}

    public Long getId() { return id; }

    public Objednavka getObjednavka() { return objednavka; }
    public void setObjednavka(Objednavka objednavka) { this.objednavka = objednavka; }

    public Produkt getProdukt() { return produkt; }
    public void setProdukt(Produkt produkt) { this.produkt = produkt; }

    public int getMnozstvi() { return mnozstvi; }
    public void setMnozstvi(int mnozstvi) { this.mnozstvi = mnozstvi; }

    public BigDecimal getCenaSnapshot() { return cenaSnapshot; }
    public void setCenaSnapshot(BigDecimal cenaSnapshot) { this.cenaSnapshot = cenaSnapshot; }
}

