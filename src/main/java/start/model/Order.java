package start.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "shop_order")
@NamedQuery(
        name = "Order.findByStatus",
        query = "SELECT o FROM Order o WHERE o.status = :status ORDER BY o.date ASC"
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq_gen")
    @SequenceGenerator(name = "order_seq_gen", sequenceName = "order_id_seq", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal dph;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(precision = 12, scale = 2)
    private BigDecimal dorights;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> item = new ArrayList<>();

    public Order() {}

    @PrePersist
    void prePersist() {
        if (date == null) date = new Date();
        if (status == null) status = OrderStatus.WAITING_FOR_CONFIRMATION;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }

    public Long getorderId() {
        return orderId;
    }

    public void setorderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getdate() {
        return date;
    }

    public void setdate(Date date) {
        this.date = date;
    }

    public OrderStatus getstatus() {
        return status;
    }

    public void setstatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDph() {
        return dph;
    }

    public void setDph(BigDecimal dph) {
        this.dph = dph;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getDorights() {
        return dorights;
    }

    public void setDorights(BigDecimal dorights) {
        this.dorights = dorights;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getitem() {
        return item;
    }

    public void setitem(List<OrderItem> item) {
        this.item = item;
    }
}