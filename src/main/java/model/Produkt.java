package model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produkt")
public class Produkt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produkt_id")
    private Long produktId;

    @Column(nullable = false)
    private String nazev;

    @Column(length = 2000)
    private String popis;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "kategorie_id")
    private Kategorie kategorie;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cena;

    @Column(nullable = false)
    private int skladem;

    @Column(nullable = false)
    private boolean dostupnost;

    public Produkt() {}

    public Long getProduktId() {
        return produktId;
    }

    public void setProduktId(Long produktId) {
        this.produktId = produktId;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public String getPopis() {
        return popis;
    }

    public void setPopis(String popis) {
        this.popis = popis;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

    public BigDecimal getCena() {
        return cena;
    }

    public void setCena(BigDecimal cena) {
        this.cena = cena;
    }

    public int getSkladem() {
        return skladem;
    }

    public void setSkladem(int skladem) {
        this.skladem = skladem;
    }

    public boolean isDostupnost() {
        return dostupnost;
    }

    public void setDostupnost(boolean dostupnost) {
        this.dostupnost = dostupnost;
    }
}