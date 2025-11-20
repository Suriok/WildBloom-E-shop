package model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrator")
public class Administrator extends User {

    private String rights;

    public Administrator() {}

    public String getrights() { return rights; }
    public void setrights(String rights) { this.rights = rights; }
}