package model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "customer")
public class Customer extends User {
    @Temporal(TemporalType.DATE)
    private Date dateRegistrace;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    public Customer() {}

    public Date getdateRegistrace() { return dateRegistrace; }
    public void setdateRegistrace(Date dateRegistrace) { this.dateRegistrace = dateRegistrace; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}
