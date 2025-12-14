package service;

import dao.*;
import model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Service
public class OrderService {

    private final CustomerDao customerDao;
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;

    private static final BigDecimal DPH_RATE = new BigDecimal("0.21"); // 21%
    private static final BigDecimal DOrights = new BigDecimal("50.00");

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED.put(OrderStatus.WAITING_FOR_CONFIRMATION, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.IN_TRANSIT, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.IN_TRANSIT, Set.of(OrderStatus.DELIVERED));
        ALLOWED.put(OrderStatus.DELIVERED, Set.of());
        ALLOWED.put(OrderStatus.CANCELLED, Set.of());
    }

    public OrderService(CustomerDao customerDao, CartDao cartDao, ProductDao productDao, OrderDao orderDao, OrderItemDao orderItemDao) {
        this.customerDao = customerDao;
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
    }

    @Transactional
    @RolesAllowed("ROLE_ZAKAZNIK")
    public Order createOrderFromCart(Long customerId) {
        final Customer z = ensureCustomer(customerId);
        final Cart k = ensureCartWithItems(z.getUserId());
        if (k.getitem().isEmpty()){
            throw new IllegalStateException("Cart is empty");
        }

        for (CartItem it : k.getitem()) {
            Product p = it.getproduct();
            if (p.getIn_stock() < it.getamount()) {
                throw new IllegalStateException("Insufficient stock: " + p.getname());
            }
        }

        Order o = new Order();
        o.setCustomer(z);
        orderDao.persist(o);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem it : k.getitem()) {
            Product p = it.getproduct();

            OrderItem po = new OrderItem();
            po.setOrder(o);
            po.setproduct(p);
            po.setamount(it.getamount());
            po.setPriceSnapshot(p.getPrice());
            orderItemDao.persist(po);
            o.getitem().add(po);

            p.setIn_stock(p.getIn_stock() - it.getamount());
            productDao.update(p);

            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(it.getamount())));
        }

        BigDecimal dph = subtotal.multiply(DPH_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(dph).add(DOrights).setScale(2, RoundingMode.HALF_UP);

        o.setDph(dph);
        o.setDorights(DOrights);
        o.setTotalAmount(total);
        orderDao.update(o);

        k.getitem().clear();
        k.settotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        cartDao.update(k);

        return o;
    }

    @Transactional
    @RolesAllowed({"ROLE_ZAKAZNIK", "ROLE_ADMINISTRATOR"})
    public void cancelOrder(Long customerId, Long orderId) {
        final Customer z = ensureCustomer(customerId);
        Order o = orderDao.find(requireNonNull(orderId));
        if (o == null){
            throw new NoSuchElementException("Order not found");
        }
        if (!o.getCustomer().getUserId().equals(z.getUserId())) {
            throw new IllegalStateException("Order does not belong to user");
        }
        if (!(o.getstatus() == OrderStatus.WAITING_FOR_CONFIRMATION || o.getstatus() == OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Cancellation not allowed for status: " + o.getstatus());
        }

        Order withItems = orderDao.findByIdWithItems(orderId);
        if (withItems == null) withItems = o;

        for (OrderItem po : withItems.getitem()) {
            Product p = po.getproduct();
            p.setIn_stock(p.getIn_stock() + po.getamount());
            productDao.update(p);
        }

        o.setstatus(OrderStatus.CANCELLED);
        orderDao.update(o);
    }

    @Transactional
    @RolesAllowed({"ROLE_PRACOVNIK", "ROLE_ADMINISTRATOR"})
    public Order changeStatus(Long orderId, OrderStatus newStatus) {
        Order o = orderDao.find(requireNonNull(orderId));
        if (o == null){
            throw new NoSuchElementException("Order not found");
        }

        final OrderStatus from = o.getstatus();
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(newStatus)) {
            throw new IllegalStateException("Invalid status transition: " + from + " -> " + newStatus);
        }
        o.setstatus(newStatus);
        return orderDao.update(o);
    }

    // helpers
    private Customer ensureCustomer(Long id) {
        Customer z = customerDao.find(requireNonNull(id));
        if (z == null){
            throw new NoSuchElementException("Customer not found");
        }
        return z;
    }

    private Cart ensureCartWithItems(Long customerId) {
        Cart k = cartDao.findByCustomerWithItems(requireNonNull(customerId));
        if (k == null){
            throw new NoSuchElementException("Cart not found");
        }
        return k;
    }
}
