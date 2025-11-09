package model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pracovnik")
public class Pracovnik extends Uzivatel {

    private String pozice;

    @Temporal(TemporalType.DATE)
    private Date datumNastupu;

    public Pracovnik() {}

    public String getPozice() { return pozice; }
    public void setPozice(String pozice) { this.pozice = pozice; }
    public Date getDatumNastupu() { return datumNastupu; }
    public void setDatumNastupu(Date datumNastupu) { this.datumNastupu = datumNastupu; }
}
