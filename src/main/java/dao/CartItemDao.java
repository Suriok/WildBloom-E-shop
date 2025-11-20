package dao;

import jakarta.persistence.NoResultException;
import model.Cart;
import model.CartItem;
import model.Product;
import org.springframework.stereotype.Repository;

@Repository
public class CartItemDao extends BaseDao<CartItem> {

    public CartItemDao() { super(CartItem.class); }

    public CartItem findByCartAndProduct(Cart cart, Product product) {
        try {
            return em.createQuery("""
                    SELECT pk FROM CartItem pk
                    WHERE pk.cart = :k AND pk.product = :p
                    """, CartItem.class)
                    .setParameter("k", cart)
                    .setParameter("p", product)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
