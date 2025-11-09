package model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(
        name = "polozka_kosiku",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kosik_produkt",
                columnNames = {"kosik_id", "produkt_id"}
        )
)
public class PolozkaKosiku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kosik_id", nullable = false)
    private Kosik kosik;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produkt_id", nullable = false)
    private Produkt produkt;

    @Column(nullable = false)
    private int mnozstvi;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public PolozkaKosiku() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = new Date();
    }

    public Long getId() { return id; }
    public Kosik getKosik() { return kosik; }
    public void setKosik(Kosik kosik) { this.kosik = kosik; }
    public Produkt getProdukt() { return produkt; }
    public void setProdukt(Produkt produkt) { this.produkt = produkt; }
    public int getMnozstvi() { return mnozstvi; }
    public void setMnozstvi(int mnozstvi) { this.mnozstvi = mnozstvi; }
    public Date getCreatedAt() { return createdAt; }
}
