package model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "order_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_product",
                columnNames = {"order_id", "product_id"}
        )
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int amount;

    @Column(name = "price_snapshot", precision = 12, scale = 2, nullable = false)
    private BigDecimal priceSnapshot;

    public OrderItem() {}

    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getproduct() { return product; }
    public void setproduct(Product product) { this.product = product; }

    public int getamount() { return amount; }
    public void setamount(int amount) { this.amount = amount; }

    public BigDecimal getPriceSnapshot() { return priceSnapshot; }
    public void setPriceSnapshot(BigDecimal priceSnapshot) { this.priceSnapshot = priceSnapshot; }
}

