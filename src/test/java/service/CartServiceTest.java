package service;

import start.dao.CartDao;
import start.dao.CartItemDao;
import start.dao.ProductDao;
import start.dao.CustomerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import start.model.*;
import start.service.CartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock private CustomerDao customerDao;
    @Mock private CartDao cartDao;
    @Mock private ProductDao productDao;
    @Mock private CartItemDao cartItemDao;
    @InjectMocks private CartService cartService;

    private Customer testCustomer;
    private Cart testCart;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setUserId(1L);
        testCustomer.setEmail("test@test.com");
        testCustomer.setName("Test User");
        testCustomer.setPassword("password");
        testCustomer.setRole(UserRole.CUSTOMER);
        testCustomer.setdateRegistrace(new Date());

        testCart = new Cart();
        testCart.setcartId(1L);
        testCart.setCustomer(testCustomer);
        testCart.setitem(new ArrayList<>());
        testCart.settotalAmount(BigDecimal.ZERO);
        testCustomer.setCart(testCart);

        testProduct1 = new Product();
        testProduct1.setproductId(1L);
        testProduct1.setPrice(new BigDecimal("100.00"));
        testProduct1.setname("Product 1");
        testProduct1.setIn_stock(100);
        testProduct1.setAvailability(true);

        testProduct2 = new Product();
        testProduct2.setproductId(2L);
        testProduct2.setPrice(new BigDecimal("50.00"));
        testProduct2.setname("Product 2");
        testProduct2.setIn_stock(50);
        testProduct2.setAvailability(true);

        testProduct3 = new Product();
        testProduct3.setproductId(3L);
        testProduct3.setPrice(new BigDecimal("25.50"));
        testProduct3.setname("Product 3");
        testProduct3.setIn_stock(5);
        testProduct3.setAvailability(true);
    }

    @Test
    void addItem_WithInvalidQuantity_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 1L, -2));
    }

    @Test
    void addItem_Twice_ShouldSumQuantities() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(2L)).thenReturn(testProduct2);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct2))).thenReturn(null);

        cartService.addItem(1L, 2L, 3);

        CartItem item = new CartItem();
        item.setCart(testCart);
        item.setproduct(testProduct2);
        item.setamount(3);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct2))).thenReturn(item);

        cartService.addItem(1L, 2L, 2);

        assertEquals(5, item.getamount());
        verify(cartItemDao, atLeastOnce()).update(any(CartItem.class));
    }

    @Test
    void updateItemQuantity_ToZero_ShouldRemoveItem() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(1L)).thenReturn(testProduct1);
        CartItem item = new CartItem();
        item.setCart(testCart);
        item.setproduct(testProduct1);
        item.setamount(4);
        testCart.getitem().add(item);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct1))).thenReturn(item);

        cartService.updateItemQuantity(1L, 1L, 0);

        verify(cartItemDao, atLeastOnce()).remove(item);
        assertFalse(testCart.getitem().contains(item));
    }

    @Test
    void addItem_CreateNewCart_WhenCustomerHasNoCart() {
        Customer customerNoCart = new Customer();
        customerNoCart.setUserId(2L);
        customerNoCart.setCart(null);
        when(customerDao.find(2L)).thenReturn(customerNoCart);
        when(cartDao.findByCustomerWithItems(2L)).thenReturn(null);
        when(productDao.find(1L)).thenReturn(testProduct1);
        doAnswer(invocation -> {
            Cart k = invocation.getArgument(0);
            k.setcartId(99L);
            customerNoCart.setCart(k);
            return null;
        }).when(cartDao).persist(any(Cart.class));
        when(cartItemDao.findByCartAndProduct(any(), eq(testProduct1))).thenReturn(null);
        when(cartDao.findByCustomerWithItems(2L)).thenReturn(customerNoCart.getCart());

        Cart resultCart = cartService.addItem(2L, 1L, 5);

        assertNotNull(resultCart);
        assertEquals(99L, resultCart.getcartId());
        assertEquals(customerNoCart, resultCart.getCustomer());
        assertTrue(resultCart.getitem().stream().anyMatch(p -> p.getproduct().equals(testProduct1)));
        verify(cartDao, times(1)).persist(any(Cart.class));
        verify(customerDao, atLeastOnce()).update(customerNoCart);
    }

    @Test
    void removeItem_AllItems_CartBecomesEmpty() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        CartItem p1 = new CartItem();
        p1.setproduct(testProduct1); p1.setamount(2); testCart.getitem().add(p1);
        CartItem p2 = new CartItem();
        p2.setproduct(testProduct2); p2.setamount(3); testCart.getitem().add(p2);
        CartItem p3 = new CartItem();
        p3.setproduct(testProduct3); p3.setamount(1); testCart.getitem().add(p3);
        when(productDao.find(1L)).thenReturn(testProduct1);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct1))).thenReturn(p1);
        cartService.removeItem(1L, 1L); testCart.getitem().remove(p1);
        when(productDao.find(2L)).thenReturn(testProduct2);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct2))).thenReturn(p2);
        cartService.removeItem(1L, 2L); testCart.getitem().remove(p2);
        when(productDao.find(3L)).thenReturn(testProduct3);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct3))).thenReturn(p3);
        cartService.removeItem(1L, 3L); testCart.getitem().remove(p3);
        assertTrue(testCart.getitem().isEmpty());
        verify(cartItemDao, times(3)).remove(any(CartItem.class));
    }

    @Test
    void removeItem_NonExisting_ShouldNotThrow() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(99L)).thenReturn(testProduct1);
        when(cartItemDao.findByCartAndProduct(eq(testCart), any())).thenReturn(null);
        assertDoesNotThrow(() -> cartService.removeItem(1L, 99L));
        verify(cartItemDao, never()).remove(any());
    }

    @Test
    void addItem_WithNullPrice_ShouldTreatAsZero() {
        Product nullPriceProduct = new Product();
        nullPriceProduct.setproductId(10L);
        nullPriceProduct.setPrice(null);
        nullPriceProduct.setname("Free product");
        nullPriceProduct.setIn_stock(100);

        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(10L)).thenReturn(nullPriceProduct);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(nullPriceProduct))).thenReturn(null);

        cartService.addItem(1L, 10L, 5);

        CartItem item = new CartItem();
        item.setproduct(nullPriceProduct);
        item.setamount(5);
        testCart.getitem().add(item);
        assertEquals(0, BigDecimal.ZERO.compareTo(testCart.gettotalAmount()));
    }

    @Test
    void addItem_NonExistingProduct_ShouldThrow() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class,
                () -> cartService.addItem(1L, 999L, 5));
    }

    @Test
    void updateItemQuantity_NonExistingItem_ShouldThrow() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(99L)).thenReturn(testProduct1);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct1))).thenReturn(null);
        assertThrows(NoSuchElementException.class,
                () -> cartService.updateItemQuantity(1L, 99L, 10));
    }

    @Test
    void addItem_WithLargeQuantity_ShouldWork() {
        testProduct1.setIn_stock(2000);

        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        when(productDao.find(1L)).thenReturn(testProduct1);
        when(cartItemDao.findByCartAndProduct(eq(testCart), eq(testProduct1))).thenReturn(null);
        int largeQuantity = 1000;
        Cart result = cartService.addItem(1L, 1L, largeQuantity);
        assertNotNull(result);
        verify(cartItemDao).persist(any(CartItem.class));
    }
}