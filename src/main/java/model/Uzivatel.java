package model;

import jakarta.persistence.*;

@Entity
@Table(name = "uzivatel")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Uzivatel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jmeno;

    private String telefon;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)          // хранить хеш
    private String heslo;

    private String adresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUzivatele role;

    protected Uzivatel() {}

    public Long getId() { return id; }
    public String getJmeno() { return jmeno; }
    public void setJmeno(String jmeno) { this.jmeno = jmeno; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getHeslo() { return heslo; }
    public void setHeslo(String heslo) { this.heslo = heslo; }
    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }
    public RoleUzivatele getRole() { return role; }
    public void setRole(RoleUzivatele role) { this.role = role; }
}
