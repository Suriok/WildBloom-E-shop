package start.dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import start.dao.exception.DaoException;
import start.model.Cart;
import start.model.Customer;
import org.springframework.stereotype.Repository;

@Repository
public class CartDao extends BaseDao<Cart> {

    public CartDao() { super(Cart.class); }

    public Cart findByCustomerWithItems(Long customerId) {
        try {
            return em.createQuery("""
                    SELECT DISTINCT k
                    FROM Cart k
                    LEFT JOIN FETCH k.item pk
                    LEFT JOIN FETCH pk.product p
                    WHERE k.customer.id = :customerId
                    """, Cart.class)
                    .setParameter("customerId", customerId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (PersistenceException e) {
            throw new DaoException("Error loading cart with items for customer id " + customerId, e);
        }
    }


}
