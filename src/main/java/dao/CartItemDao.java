package dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import dao.exception.DaoException;
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
        }catch (PersistenceException e) {
            throw new DaoException("Error finding cart item by cart and product", e);
        }
    }

}
