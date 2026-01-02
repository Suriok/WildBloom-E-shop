package start.dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import start.dao.exception.DaoException;
import start.model.Order;
import start.model.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDao extends BaseDao<Order> {

    public OrderDao() { super(Order.class); }

    public List<Order> findBycustomerId(Long customerId) {
        try {
            return em.createQuery("""
                
                            SELECT o FROM Order o
                WHERE o.customer.id = :customerId
                ORDER BY o.date DESC
                """, Order.class)
                .setParameter("customerId", customerId)
                .getResultList();
        }catch (PersistenceException e) {
            throw new DaoException("Error finding orders by customerId " + customerId, e);
        }
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
        }catch (PersistenceException e) {
            throw new DaoException("Error finding orders by id " + orderId, e);
        }
    }
}




