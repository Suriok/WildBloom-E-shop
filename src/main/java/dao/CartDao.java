package dao;

import jakarta.persistence.NoResultException;
import model.Cart;
import model.Customer;
import org.springframework.stereotype.Repository;

@Repository
public class CartDao extends BaseDao<Cart> {

    public CartDao() { super(Cart.class); }

    public Cart findByCustomer(Customer customer) {
        try {
            return em.createQuery("SELECT k FROM Cart k WHERE k.customer = :z", Cart.class)
                    .setParameter("z", customer)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

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
        }
    }


}
