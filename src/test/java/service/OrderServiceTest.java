package service;

import dao.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private ZakaznikDao zakaznikDao;
    @Mock private KosikDao kosikDao;
    @Mock private ProduktDao produktDao;
    @Mock private ObjednavkaDao objednavkaDao;
    @Mock private PolozkaObjednavkyDao polozkaObjednavkyDao;
    @InjectMocks private OrderService orderService;

    private Zakaznik testCustomer;
    private Kosik testCart;
    private Produkt testProduct1;
    private Produkt testProduct2;

    @BeforeEach
    void setUp() {
        testCustomer = new Zakaznik();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        testCustomer.setJmeno("John Doe");
        testCustomer.setHeslo("password");
        testCustomer.setRole(RoleUzivatele.ZAKAZNIK);
        testCustomer.setDatumRegistrace(new Date());

        testCart = new Kosik();
        testCart.setKosikId(1L);
        testCart.setZakaznik(testCustomer);
        testCart.setPolozky(new ArrayList<>());
        testCart.setCelkovaSuma(BigDecimal.ZERO);

        testProduct1 = new Produkt();
        testProduct1.setProduktId(1L);
        testProduct1.setCena(new BigDecimal("100.00"));
        testProduct1.setNazev("Product A");
        testProduct1.setSkladem(10);
        testProduct1.setDostupnost(true);

        testProduct2 = new Produkt();
        testProduct2.setProduktId(2L);
        testProduct2.setCena(new BigDecimal("50.00"));
        testProduct2.setNazev("Product B");
        testProduct2.setSkladem(5);
        testProduct2.setDostupnost(true);
    }

    @Test
    void createOrderFromCart_WithEmptyCart_ShouldThrow() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart(1L));
    }

    @Test
    void createOrderFromCart_WithNonExistentCustomer_ShouldThrow() {
        when(zakaznikDao.find(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> orderService.createOrderFromCart(999L));
    }

    @Test
    void createOrderFromCart_WithInsufficientStock_ShouldThrow() {
        PolozkaKosiku item = new PolozkaKosiku();
        item.setProdukt(testProduct1);
        item.setMnozstvi(20);
        testCart.getPolozky().add(item);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart(1L));
    }

    @Test
    void createOrderFromCart_WithValidCart_CreatesOrderAndClearsCart() {
        PolozkaKosiku item1 = new PolozkaKosiku();
        item1.setProdukt(testProduct1);
        item1.setMnozstvi(2);
        PolozkaKosiku item2 = new PolozkaKosiku();
        item2.setProdukt(testProduct2);
        item2.setMnozstvi(3);
        testCart.getPolozky().add(item1);
        testCart.getPolozky().add(item2);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Objednavka o = invocation.getArgument(0);
            o.setObjednavkaId(100L);
            o.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
            o.setPolozky(new ArrayList<>());
            return null;
        }).when(objednavkaDao).persist(any(Objednavka.class));
        Objednavka result = orderService.createOrderFromCart(1L);
        assertNotNull(result);
        assertEquals(testCustomer, result.getZakaznik());
        assertEquals(StavObjednavky.CEKA_NA_POTVRZENI, result.getStav());
        BigDecimal subtotal = new BigDecimal("100.00").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(3)));
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = new BigDecimal("50.00");
        BigDecimal expectedTotal = subtotal.add(vat).add(shipping).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTotal, result.getCelkovaCena());
        assertEquals(vat, result.getDph());
        assertEquals(shipping, result.getDoprava());
        assertTrue(testCart.getPolozky().isEmpty());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), testCart.getCelkovaSuma());
        verify(produktDao, times(2)).update(any(Produkt.class));
        verify(polozkaObjednavkyDao, times(2)).persist(any(PolozkaObjednavky.class));
        verify(kosikDao, times(1)).update(testCart);
    }

    @Test
    void createOrderFromCart_DecreasesStockCorrectly() {
        PolozkaKosiku item = new PolozkaKosiku();
        item.setProdukt(testProduct1);
        item.setMnozstvi(3);
        testCart.getPolozky().add(item);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Objednavka o = invocation.getArgument(0);
            o.setObjednavkaId(100L);
            o.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
            o.setPolozky(new ArrayList<>());
            return null;
        }).when(objednavkaDao).persist(any(Objednavka.class));
        orderService.createOrderFromCart(1L);
        assertEquals(7, testProduct1.getSkladem());
    }

    @Test
    void cancelOrder_WithNonExistentOrder_ShouldThrow() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> orderService.cancelOrder(1L, 999L));
    }

    @Test
    void cancelOrder_WithOrderNotBelongingToCustomer_ShouldThrow() {
        Zakaznik otherCustomer = new Zakaznik();
        otherCustomer.setId(2L);
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setZakaznik(otherCustomer);
        order.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(10L)).thenReturn(order);
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L, 10L));
    }

    @Test
    void cancelOrder_WithInvalidStatus_ShouldThrow() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setZakaznik(testCustomer);
        order.setStav(StavObjednavky.DORUCENO);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(10L)).thenReturn(order);
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L, 10L));
    }

    @Test
    void cancelOrder_WithValidOrder_CancelsAndRestoresStock() {
        PolozkaObjednavky orderItem = new PolozkaObjednavky();
        orderItem.setProdukt(testProduct1);
        orderItem.setMnozstvi(3);
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setZakaznik(testCustomer);
        order.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
        order.setPolozky(new ArrayList<>(List.of(orderItem)));
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(10L)).thenReturn(order);
        when(objednavkaDao.findByIdWithItems(10L)).thenReturn(order);
        int initialStock = testProduct1.getSkladem();
        orderService.cancelOrder(1L, 10L);
        assertEquals(StavObjednavky.ZRUSENO, order.getStav());
        assertEquals(initialStock + 3, testProduct1.getSkladem());
        verify(produktDao, times(1)).update(testProduct1);
        verify(objednavkaDao, times(1)).update(order);
    }

    @Test
    void cancelOrder_FromConfirmedStatus_AllowsCancellation() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setZakaznik(testCustomer);
        order.setStav(StavObjednavky.POTVRZENO);
        order.setPolozky(new ArrayList<>());
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(10L)).thenReturn(order);
        when(objednavkaDao.findByIdWithItems(10L)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.cancelOrder(1L, 10L));
        assertEquals(StavObjednavky.ZRUSENO, order.getStav());
    }

    @Test
    void changeStatus_WithNonExistentOrder_ShouldThrow() {
        when(objednavkaDao.find(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class,
                () -> orderService.changeStatus(999L, StavObjednavky.POTVRZENO));
    }

    @Test
    void changeStatus_WithInvalidTransition_ShouldThrow() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
        when(objednavkaDao.find(10L)).thenReturn(order);
        assertThrows(IllegalStateException.class,
                () -> orderService.changeStatus(10L, StavObjednavky.DORUCENO));
    }



    @Test
    void changeStatus_AllowedTransitions_FromConfirmed() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setStav(StavObjednavky.POTVRZENO);
        when(objednavkaDao.find(10L)).thenReturn(order);
        when(objednavkaDao.update(order)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, StavObjednavky.V_DOPRAVE));
        order.setStav(StavObjednavky.POTVRZENO);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, StavObjednavky.ZRUSENO));
    }

    @Test
    void changeStatus_AllowedTransitions_FromInTransit() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setStav(StavObjednavky.V_DOPRAVE);
        when(objednavkaDao.find(10L)).thenReturn(order);
        when(objednavkaDao.update(order)).thenReturn(order);
        assertDoesNotThrow(() -> orderService.changeStatus(10L, StavObjednavky.DORUCENO));
    }

    @Test
    void changeStatus_NoTransitionsAllowed_FromDelivered() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setStav(StavObjednavky.DORUCENO);
        when(objednavkaDao.find(10L)).thenReturn(order);
        assertThrows(IllegalStateException.class,
                () -> orderService.changeStatus(10L, StavObjednavky.ZRUSENO));
    }

    @Test
    void changeStatus_NoTransitionsAllowed_FromCancelled() {
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setStav(StavObjednavky.ZRUSENO);
        when(objednavkaDao.find(10L)).thenReturn(order);
        assertThrows(IllegalStateException.class,
                () -> orderService.changeStatus(10L, StavObjednavky.POTVRZENO));
    }

    @Test
    void createOrderFromCart_WithMultipleProducts_CalculatesCorrectTotal() {
        PolozkaKosiku item1 = new PolozkaKosiku();
        item1.setProdukt(testProduct1);
        item1.setMnozstvi(2);
        PolozkaKosiku item2 = new PolozkaKosiku();
        item2.setProdukt(testProduct2);
        item2.setMnozstvi(4);
        testCart.getPolozky().add(item1);
        testCart.getPolozky().add(item2);
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        doAnswer(invocation -> {
            Objednavka o = invocation.getArgument(0);
            o.setObjednavkaId(100L);
            o.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
            o.setPolozky(new ArrayList<>());
            return null;
        }).when(objednavkaDao).persist(any(Objednavka.class));
        Objednavka result = orderService.createOrderFromCart(1L);
        BigDecimal subtotal = new BigDecimal("100.00").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(4)));
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = subtotal.add(vat).add(new BigDecimal("50.00"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTotal, result.getCelkovaCena());
    }

    @Test
    void cancelOrder_WithMultipleItems_RestoresAllStock() {
        PolozkaObjednavky item1 = new PolozkaObjednavky();
        item1.setProdukt(testProduct1);
        item1.setMnozstvi(2);
        PolozkaObjednavky item2 = new PolozkaObjednavky();
        item2.setProdukt(testProduct2);
        item2.setMnozstvi(3);
        Objednavka order = new Objednavka();
        order.setObjednavkaId(10L);
        order.setZakaznik(testCustomer);
        order.setStav(StavObjednavky.CEKA_NA_POTVRZENI);
        order.setPolozky(new ArrayList<>(List.of(item1, item2)));
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(objednavkaDao.find(10L)).thenReturn(order);
        when(objednavkaDao.findByIdWithItems(10L)).thenReturn(order);
        int stock1Before = testProduct1.getSkladem();
        int stock2Before = testProduct2.getSkladem();
        orderService.cancelOrder(1L, 10L);
        assertEquals(stock1Before + 2, testProduct1.getSkladem());
        assertEquals(stock2Before + 3, testProduct2.getSkladem());
        verify(produktDao, times(2)).update(any(Produkt.class));
    }
}
