package dao;

import jakarta.persistence.NoResultException;
import model.Order;
import model.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDao extends BaseDao<Order> {

    public OrderDao() { super(Order.class); }

    public List<Order> findByStatusOrderByDateAsc(OrderStatus status) {
        return em.createQuery("""
                SELECT o FROM Order o
                WHERE o.status = :status
                ORDER BY o.date ASC
                """, Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<Order> findBycustomerId(Long customerId) {
        return em.createQuery("""
                SELECT o FROM Order o
                WHERE o.customer.id = :customerId
                ORDER BY o.date DESC
                """, Order.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    public Order findByIdWithItems(Long orderId) {
        try {
            return em.createQuery("""
                    SELECT DISTINCT o
                    FROM Order o
                    LEFT JOIN FETCH o.item po
                    LEFT JOIN FETCH po.product p
                    WHERE o.orderId = :id
                    """, Order.class)
                    .setParameter("id", orderId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}




