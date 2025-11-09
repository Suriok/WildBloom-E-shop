package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kategorie")
public class Kategorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kategorie_id")
    private Long kategorieId;

    @Column(nullable = false, unique = true)
    private String nazev;

    @OneToMany(mappedBy = "kategorie", fetch = FetchType.LAZY)
    @OrderBy("nazev ASC")        // как в сценарии
    private List<Produkt> produkty = new ArrayList<>();

    public Kategorie() {}

    public Long getKategorieId() { return kategorieId; }
    public String getNazev() { return nazev; }
    public void setNazev(String nazev) { this.nazev = nazev; }
    public List<Produkt> getProdukty() { return produkty; }
}
