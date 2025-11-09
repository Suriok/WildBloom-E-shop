package model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "zakaznik")
public class Zakaznik extends Uzivatel{
    @Temporal(TemporalType.DATE)
    private Date datumRegistrace;

    @OneToOne(mappedBy = "zakaznik", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    private Kosik kosik;

    public Zakaznik() {}

    public Date getDatumRegistrace() { return datumRegistrace; }
    public void setDatumRegistrace(Date datumRegistrace) { this.datumRegistrace = datumRegistrace; }
    public Kosik getKosik() { return kosik; }
    public void setKosik(Kosik kosik) { this.kosik = kosik; }
}
