package model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrator")
public class Administrator extends Uzivatel {

    private String prava;

    public Administrator() {}

    public String getPrava() { return prava; }
    public void setPrava(String prava) { this.prava = prava; }
}