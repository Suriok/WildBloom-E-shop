package start.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(
        name = "cart_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_item",
                columnNames = {"cart_id", "product_id"}
        )
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cartItem_seq_gen")
    @SequenceGenerator(name = "cartItem_seq_gen", sequenceName = "cartItem_id_seq", allocationSize = 1)
    @Column(name = "cartItem_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int amount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public CartItem() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = new Date();
    }

    public Long getCartItemId() { return id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Product getproduct() { return product; }
    public void setproduct(Product product) { this.product = product; }
    public int getamount() { return amount; }
    public void setamount(int amount) { this.amount = amount; }
    public Date getCreatedAt() { return createdAt; }
}
