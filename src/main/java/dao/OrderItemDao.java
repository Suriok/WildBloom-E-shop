package dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import dao.exception.DaoException;
import model.Order;
import model.OrderItem;
import model.Product;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemDao extends BaseDao<OrderItem> {

    public OrderItemDao() { super(OrderItem.class); }

    public OrderItem findByOrderAndProduct(Order order, Product product) {
        try {
            return em.createQuery("""
                    SELECT po FROM OrderItem po
                    WHERE po.order = :o AND po.product = :p
                    """, OrderItem.class)
                    .setParameter("o", order)
                    .setParameter("p", product)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }catch (PersistenceException e) {
            throw new DaoException("Error finding order item.", e);
        }
    }

    public boolean existsByOrderAndProduct(Order order, Product product) {
        try{
        Long cnt = em.createQuery("""
                SELECT COUNT(po) FROM OrderItem po
                WHERE po.order = :o AND po.product = :p
                """, Long.class)
                .setParameter("o", order)
                .setParameter("p", product)
                .getSingleResult();
        return cnt != null && cnt > 0;
        }catch (PersistenceException e) {
            throw new DaoException("Error checking existence of order item.", e);
        }
    }
}

