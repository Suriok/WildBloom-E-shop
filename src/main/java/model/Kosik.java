package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "kosik")
@NamedQuery(
        name = "Kosik.findByZakaznik",
        query = "SELECT k FROM Kosik k LEFT JOIN FETCH k.polozky pk LEFT JOIN FETCH pk.produkt WHERE k.zakaznik.id = :zakaznikId"
)
public class Kosik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kosik_id")
    private Long kosikId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datumVzniku;

    @Column(precision = 12, scale = 2)
    private BigDecimal celkovaSuma;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zakaznik_id", unique = true)
    private Zakaznik zakaznik;

    @OneToMany(mappedBy = "kosik", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")           // “позиции упорядочены по дате добавления”
    private List<PolozkaKosiku> polozky = new ArrayList<>();

    public Kosik() {}

    @PrePersist
    void prePersist() {
        if (datumVzniku == null) datumVzniku = new Date();
        if (celkovaSuma == null) celkovaSuma = BigDecimal.ZERO;
    }

    public Long getKosikId() { return kosikId; }
    public Date getDatumVzniku() { return datumVzniku; }
    public BigDecimal getCelkovaSuma() { return celkovaSuma; }
    public void setCelkovaSuma(BigDecimal celkovaSuma) { this.celkovaSuma = celkovaSuma; }
    public Zakaznik getZakaznik() { return zakaznik; }
    public void setZakaznik(Zakaznik zakaznik) { this.zakaznik = zakaznik; }
    public List<PolozkaKosiku> getPolozky() { return polozky; }
}