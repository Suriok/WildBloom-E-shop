package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import start.dao.*;
import start.model.*;
import start.service.OrderService;
import start.service.exception.BusinessException;
import start.service.exception.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private CustomerDao customerDao;
    @Mock private CartDao cartDao;
    @Mock private ProductDao productDao;
    @Mock private OrderDao orderDao;
    @Mock private OrderItemDao orderItemDao;
    @InjectMocks private OrderService orderService;

    private Customer testCustomer;
    private Cart testCart;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setUserId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setName("John Doe");
        testCustomer.setPassword("password");
        testCustomer.setRole(UserRole.CUSTOMER);
        testCustomer.setdateRegistrace(new Date());

        testCart = new Cart();
        testCart.setcartId(1L);
        testCart.setCustomer(testCustomer);
        testCart.setitem(new ArrayList<>());
        testCart.settotalAmount(BigDecimal.ZERO);

        testProduct1 = new Product();
        testProduct1.setProductId(1L);
        testProduct1.setPrice(new BigDecimal("100.00"));
        testProduct1.setName("Product A");
        testProduct1.setin_stock(10);
        testProduct1.setAvailability(true);

        testProduct2 = new Product();
        testProduct2.setProductId(2L);
        testProduct2.setPrice(new BigDecimal("50.00"));
        testProduct2.setName("Product B");
        testProduct2.setin_stock(10);
        testProduct2.setAvailability(true);
    }

    @Test
    void createOrderFromCart_WithEmptyCart_ShouldThrow() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(1L));
    }

    @Test
    void createOrderFromCart_WithNonExistentCustomer_ShouldThrow() {
        when(customerDao.find(999L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> orderService.createOrderFromCart(999L));
    }

    @Test
    void createOrderFromCart_WithInsufficientStock_ShouldThrow() {
        CartItem item = new CartItem();
        item.setproduct(testProduct1);
        item.setamount(20); // Пытаемся купить 20, а на складе 10 (см. setUp)
        testCart.getitem().add(item);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(1L));
    }

    @Test
    void createOrderFromCart_WithValidCart_CreatesOrderAndClearsCart() {
        CartItem item1 = new CartItem();
        item1.setproduct(testProduct1);
        item1.setamount(2);
        CartItem item2 = new CartItem();
        item2.setproduct(testProduct2);
        item2.setamount(3);
        testCart.getitem().add(item1);
        testCart.getitem().add(item2);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setorderId(100L);
            o.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
            o.setitem(new ArrayList<>());
            return null;
        }).when(orderDao).persist(any(Order.class));
        Order result = orderService.createOrderFromCart(1L);
        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        assertEquals(OrderStatus.WAITING_FOR_CONFIRMATION, result.getstatus());
        BigDecimal subtotal = new BigDecimal("100.00").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(3)));
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = new BigDecimal("50.00");
        BigDecimal expectedTotal = subtotal.add(vat).add(shipping).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTotal, result.getTotalAmount());
        assertEquals(vat, result.getDph());
        assertEquals(shipping, result.getDorights());
        assertTrue(testCart.getitem().isEmpty());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), testCart.gettotalAmount());
        verify(productDao, times(2)).update(any(Product.class));
        verify(orderItemDao, times(2)).persist(any(OrderItem.class));
        verify(cartDao, times(1)).update(testCart);
    }

    @Test
    void createOrderFromCart_DecreasesStockCorrectly() {
        CartItem item = new CartItem();
        item.setproduct(testProduct1);
        item.setamount(3);
        testCart.getitem().add(item);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setorderId(100L);
            o.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
            o.setitem(new ArrayList<>());
            return null;
        }).when(orderDao).persist(any(Order.class));
        orderService.createOrderFromCart(1L);
        assertEquals(7, testProduct1.getin_stock());
    }

    @Test
    void cancelOrder_WithNonExistentOrder_ShouldThrow() {
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(999L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> orderService.cancelOrder(1L, 999L));
    }

    @Test
    void cancelOrder_WithOrderNotBelongingToCustomer_ShouldThrow() {
        Customer otherCustomer = new Customer();
        otherCustomer.setUserId(2L);
        Order order = new Order();
        order.setorderId(10L);
        order.setCustomer(otherCustomer);
        order.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(10L)).thenReturn(order);
        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 10L));
    }

    @Test
    void cancelOrder_WithInvalidStatus_ShouldThrow() {
        Order order = new Order();
        order.setorderId(10L);
        order.setCustomer(testCustomer);
        order.setstatus(OrderStatus.DELIVERED);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(10L)).thenReturn(order);
        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 10L));
    }

    @Test
    void cancelOrder_WithValidOrder_CancelsAndRestoresStock() {
        OrderItem orderItem = new OrderItem();
        orderItem.setproduct(testProduct1);
        orderItem.setamount(3);
        Order order = new Order();
        order.setorderId(10L);
        order.setCustomer(testCustomer);
        order.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
        order.setitem(new ArrayList<>(List.of(orderItem)));
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(10L)).thenReturn(order);
        when(orderDao.findByIdWithItems(10L)).thenReturn(order);
        int initialStock = testProduct1.getin_stock();
        orderService.cancelOrder(1L, 10L);
        assertEquals(OrderStatus.CANCELLED, order.getstatus());
        assertEquals(initialStock + 3, testProduct1.getin_stock());
        verify(productDao, times(1)).update(testProduct1);
        verify(orderDao, times(1)).update(order);
    }

    @Test
    void cancelOrder_FromConfirmedStatus_AllowsCancellation() {
        Order order = new Order();
        order.setorderId(10L);
        order.setCustomer(testCustomer);
        order.setstatus(OrderStatus.CONFIRMED);
        order.setitem(new ArrayList<>());
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(10L)).thenReturn(order);
        when(orderDao.findByIdWithItems(10L)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.cancelOrder(1L, 10L));
        assertEquals(OrderStatus.CANCELLED, order.getstatus());
    }

    @Test
    void changeStatus_WithNonExistentOrder_ShouldThrow() {
        when(orderDao.find(999L)).thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> orderService.changeStatus(999L, OrderStatus.CONFIRMED));
    }

    @Test
    void changeStatus_WithInvalidTransition_ShouldThrow() {
        Order order = new Order();
        order.setorderId(10L);
        order.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
        when(orderDao.find(10L)).thenReturn(order);
        assertThrows(BusinessException.class,
                () -> orderService.changeStatus(10L, OrderStatus.DELIVERED));
    }

    @Test
    void changeStatus_AllowedTransitions_FromConfirmed() {
        Order order = new Order();
        order.setorderId(10L);
        order.setstatus(OrderStatus.CONFIRMED);
        when(orderDao.find(10L)).thenReturn(order);
        when(orderDao.update(order)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, OrderStatus.IN_TRANSIT));
        order.setstatus(OrderStatus.CONFIRMED);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, OrderStatus.CANCELLED));
    }

    @Test
    void changeStatus_AllowedTransitions_FromInTransit() {
        Order order = new Order();
        order.setorderId(10L);
        order.setstatus(OrderStatus.IN_TRANSIT);
        when(orderDao.find(10L)).thenReturn(order);
        when(orderDao.update(order)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, OrderStatus.DELIVERED));
    }

    @Test
    void changeStatus_NoTransitionsAllowed_FromDelivered() {
        Order order = new Order();
        order.setorderId(10L);
        order.setstatus(OrderStatus.DELIVERED);
        when(orderDao.find(10L)).thenReturn(order);
        assertThrows(BusinessException.class,
                () -> orderService.changeStatus(10L, OrderStatus.CANCELLED));
    }

    @Test
    void changeStatus_NoTransitionsAllowed_FromCancelled() {
        Order order = new Order();
        order.setorderId(10L);
        order.setstatus(OrderStatus.CANCELLED);
        when(orderDao.find(10L)).thenReturn(order);
        assertThrows(BusinessException.class,
                () -> orderService.changeStatus(10L, OrderStatus.CONFIRMED));
    }

    @Test
    void createOrderFromCart_WithMultipleProducts_CalculatesCorrectTotal() {
        CartItem item1 = new CartItem();
        item1.setproduct(testProduct1);
        item1.setamount(2);
        CartItem item2 = new CartItem();
        item2.setproduct(testProduct2);
        item2.setamount(4);
        testCart.getitem().add(item1);
        testCart.getitem().add(item2);
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(cartDao.findByCustomerWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setorderId(100L);
            o.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
            o.setitem(new ArrayList<>());
            return null;
        }).when(orderDao).persist(any(Order.class));
        Order result = orderService.createOrderFromCart(1L);
        BigDecimal subtotal = new BigDecimal("100.00").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(4)));
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = subtotal.add(vat).add(new BigDecimal("50.00"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTotal, result.getTotalAmount());
    }

    @Test
    void cancelOrder_WithMultipleItems_RestoresAllStock() {
        OrderItem item1 = new OrderItem();
        item1.setproduct(testProduct1);
        item1.setamount(2);
        OrderItem item2 = new OrderItem();
        item2.setproduct(testProduct2);
        item2.setamount(3);
        Order order = new Order();
        order.setorderId(10L);
        order.setCustomer(testCustomer);
        order.setstatus(OrderStatus.WAITING_FOR_CONFIRMATION);
        order.setitem(new ArrayList<>(List.of(item1, item2)));
        when(customerDao.find(1L)).thenReturn(testCustomer);
        when(orderDao.find(10L)).thenReturn(order);
        when(orderDao.findByIdWithItems(10L)).thenReturn(order);
        int stock1Before = testProduct1.getin_stock();
        int stock2Before = testProduct2.getin_stock();
        orderService.cancelOrder(1L, 10L);
        assertEquals(stock1Before + 2, testProduct1.getin_stock());
        assertEquals(stock2Before + 3, testProduct2.getin_stock());
        verify(productDao, times(2)).update(any(Product.class));
    }
}