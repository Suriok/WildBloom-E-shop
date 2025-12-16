package start.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Cart")
@NamedQuery(
        name = "Cart.findByCustomer",
        query = "SELECT k FROM Cart k LEFT JOIN FETCH k.item pk LEFT JOIN FETCH pk.product WHERE k.customer.id = :customerId"
)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfCreation;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<CartItem> item = new ArrayList<>();

    public Cart() {}

    @PrePersist
    void prePersist() {
        if (dateOfCreation == null) dateOfCreation = new Date();
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }

    public Long getcartId() {
        return cartId;
    }

    public void setcartId(Long cartId) {
        this.cartId = cartId;
    }

    public Date getdateOfCreation() {
        return dateOfCreation;
    }

    public void setdateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public BigDecimal gettotalAmount() {
        return totalAmount;
    }

    public void settotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<CartItem> getitem() {
        return item;
    }

    public void setitem(List<CartItem> item) {
        this.item = item;
    }
}