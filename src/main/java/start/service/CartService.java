package start.service;

import start.dao.CartDao;
import start.dao.CartItemDao;
import start.dao.ProductDao;
import start.dao.CustomerDao;
import start.model.Cart;
import start.model.CartItem;
import start.model.Customer;
import start.model.Product;
import start.service.exception.BusinessException;
import start.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

@Service
public class CartService {

    private final CustomerDao customerDao;
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final CartItemDao cartItemDao;

    public CartService(CustomerDao customerDao, CartDao cartDao, ProductDao productDao, CartItemDao cartItemDao) {
        this.customerDao = customerDao;
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.cartItemDao = cartItemDao;
    }

    @Transactional
    public Cart addItem(Long customerId, Long productId, int amount) {
        if (amount <= 0){
            throw new IllegalArgumentException("Amount must be > 0");
        }
        final Customer z = ensureCustomer(customerId);
        Cart k = cartDao.findByCustomerWithItems(z.getUserId());
        if (k == null){
            k = ensureCart(z);
        }

        final Product p = ensureproduct(productId);

        if (p.getin_stock() < amount) {
            throw new BusinessException("Lack of goods in stock. Available: " + p.getin_stock());
        }

        CartItem pk = cartItemDao.findByCartAndProduct(k, p);
        if (pk == null) {
            pk = new CartItem();
            pk.setCart(k);
            pk.setproduct(p);
            pk.setamount(amount);
            cartItemDao.persist(pk);
            k.getitem().add(pk);
        } else {
            int desired = pk.getamount() + amount;

            if (p.getin_stock() < desired) {
                throw new BusinessException(
                        "Lack of goods in stock. You already have in cart: " + pk.getamount()
                                + ", available: " + p.getin_stock()
                );
            }

            pk.setamount(desired);
            cartItemDao.update(pk);
        }
        recalculateCartTotal(k);
        cartDao.update(k);
        return k;
    }

    @Transactional
    public Cart updateItemQuantity(Long customerId, Long productId, int newQty) {
        if (newQty < 0){
            throw new IllegalArgumentException("Amount must be > 0");
        }
        final Customer z = ensureCustomer(customerId);
        final Cart k = ensureCartFor(z);
        final Product p = ensureproduct(productId);

        CartItem pk = cartItemDao.findByCartAndProduct(k, p);
        if (pk == null){
            throw new NotFoundException("Cart item", "cartId=" + k.getcartId() + ", productId=" + productId);
        }

        if (newQty > p.getin_stock()) {
            throw new BusinessException(
                    "Lack of goods in stock. Available: " + p.getin_stock()
            );
        }

        if (newQty == 0) {
            k.getitem().remove(pk);
            cartItemDao.remove(pk);
        } else {
            pk.setamount(newQty);
            cartItemDao.update(pk);
        }
        recalculateCartTotal(k);
        cartDao.update(k);
        return k;
    }

    @Transactional
    public Cart removeItem(Long customerId, Long productId) {
        final Customer z = ensureCustomer(customerId);
        final Cart k = ensureCartFor(z);
        final Product p = ensureproduct(productId);

        CartItem pk = cartItemDao.findByCartAndProduct(k, p);
        if (pk != null) {
            k.getitem().remove(pk);
            cartItemDao.remove(pk);
            recalculateCartTotal(k);
            cartDao.update(k);
        }
        return k;
    }

    // helpers
    private Customer ensureCustomer(Long id) {
        Customer z = customerDao.find(requireNonNull(id));
        if (z == null){
            throw new NotFoundException("Customer", id);
        }
        return z;
    }

    private Cart ensureCart(Customer z) {
        Cart k = z.getCart();
        if (k == null) {
            k = new Cart();
            k.setCustomer(z);
            cartDao.persist(k);
            z.setCart(k);
            customerDao.update(z);
        }
        return k;
    }

    private Cart ensureCartFor(Customer z) {
        Cart k = cartDao.findByCustomerWithItems(z.getUserId());
        if (k == null){
            k = ensureCart(z);
        }
        return k;
    }

    private Product ensureproduct(Long productId) {
        Product p = productDao.find(requireNonNull(productId));
        if (p == null){
            throw new NotFoundException("Product", productId);
        }
        return p;
    }

    private void recalculateCartTotal(Cart k) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItem it : k.getitem()) {
            BigDecimal price = it.getproduct().getPrice();
            if (price == null){
                price = BigDecimal.ZERO;
            }
            sum = sum.add(price.multiply(BigDecimal.valueOf(it.getamount())));
        }
        k.settotalAmount(sum.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional
    public Cart getOrCreateCart(Long customerId) {
        final Customer z = ensureCustomer(customerId);
        return ensureCartFor(z);
    }

}
