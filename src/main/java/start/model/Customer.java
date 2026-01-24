package start.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "customer")
public class Customer extends User {
    @Temporal(TemporalType.DATE)
    private Date registrationDate;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    public Customer() {}

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}
