package service;

import dao.KosikDao;
import dao.PolozkaKosikuDao;
import dao.ProduktDao;
import dao.ZakaznikDao;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock private ZakaznikDao zakaznikDao;
    @Mock private KosikDao kosikDao;
    @Mock private ProduktDao produktDao;
    @Mock private PolozkaKosikuDao polozkaKosikuDao;
    @InjectMocks private CartService cartService;

    private Zakaznik testCustomer;
    private Kosik testCart;
    private Produkt testProduct1;
    private Produkt testProduct2;
    private Produkt testProduct3;

    @BeforeEach
    void setUp() {
        testCustomer = new Zakaznik();
        testCustomer.setId(1L);
        testCustomer.setEmail("test@test.com");
        testCustomer.setJmeno("Test User");
        testCustomer.setHeslo("password");
        testCustomer.setRole(RoleUzivatele.ZAKAZNIK);
        testCustomer.setDatumRegistrace(new Date());

        testCart = new Kosik();
        testCart.setKosikId(1L);
        testCart.setZakaznik(testCustomer);
        testCart.setPolozky(new ArrayList<>());
        testCart.setCelkovaSuma(BigDecimal.ZERO);
        testCustomer.setKosik(testCart);

        testProduct1 = new Produkt();
        testProduct1.setProduktId(1L);
        testProduct1.setCena(new BigDecimal("100.00"));
        testProduct1.setNazev("Product 1");

        testProduct2 = new Produkt();
        testProduct2.setProduktId(2L);
        testProduct2.setCena(new BigDecimal("50.00"));
        testProduct2.setNazev("Product 2");

        testProduct3 = new Produkt();
        testProduct3.setProduktId(3L);
        testProduct3.setCena(new BigDecimal("25.50"));
        testProduct3.setNazev("Product 3");
    }

    @Test
    void addItem_WithInvalidQuantity_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 1L, -2));
    }

    @Test
    void addItem_Twice_ShouldSumQuantities() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(2L)).thenReturn(testProduct2);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct2))).thenReturn(null);
        cartService.addItem(1L, 2L, 3);
        PolozkaKosiku item = new PolozkaKosiku();
        item.setKosik(testCart);
        item.setProdukt(testProduct2);
        item.setMnozstvi(3);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct2))).thenReturn(item);
        cartService.addItem(1L, 2L, 2);
        assertEquals(5, item.getMnozstvi());
        verify(polozkaKosikuDao, atLeastOnce()).update(any(PolozkaKosiku.class));
    }

    @Test
    void updateItemQuantity_ToZero_ShouldRemoveItem() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(1L)).thenReturn(testProduct1);
        PolozkaKosiku item = new PolozkaKosiku();
        item.setKosik(testCart);
        item.setProdukt(testProduct1);
        item.setMnozstvi(4);
        testCart.getPolozky().add(item);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct1))).thenReturn(item);
        cartService.updateItemQuantity(1L, 1L, 0);
        verify(polozkaKosikuDao, atLeastOnce()).remove(item);
        assertFalse(testCart.getPolozky().contains(item));
    }

    @Test
    void addItem_CreateNewCart_WhenCustomerHasNoCart() {
        Zakaznik customerNoCart = new Zakaznik();
        customerNoCart.setId(2L);
        customerNoCart.setKosik(null);
        when(zakaznikDao.find(2L)).thenReturn(customerNoCart);
        when(kosikDao.findByZakaznikWithItems(2L)).thenReturn(null);
        when(produktDao.find(1L)).thenReturn(testProduct1);
        doAnswer(invocation -> {
            Kosik k = invocation.getArgument(0);
            k.setKosikId(99L);
            customerNoCart.setKosik(k);
            return null;
        }).when(kosikDao).persist(any(Kosik.class));
        when(polozkaKosikuDao.findByKosikAndProdukt(any(), eq(testProduct1))).thenReturn(null);
        when(kosikDao.findByZakaznikWithItems(2L)).thenReturn(customerNoCart.getKosik());
        Kosik resultCart = cartService.addItem(2L, 1L, 5);
        assertNotNull(resultCart);
        assertEquals(99L, resultCart.getKosikId());
        assertEquals(customerNoCart, resultCart.getZakaznik());
        assertTrue(resultCart.getPolozky().stream().anyMatch(p -> p.getProdukt().equals(testProduct1)));
        verify(kosikDao, times(1)).persist(any(Kosik.class));
        verify(zakaznikDao, atLeastOnce()).update(customerNoCart);
    }

    @Test
    void removeItem_AllItems_CartBecomesEmpty() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        PolozkaKosiku p1 = new PolozkaKosiku();
        p1.setProdukt(testProduct1); p1.setMnozstvi(2); testCart.getPolozky().add(p1);
        PolozkaKosiku p2 = new PolozkaKosiku();
        p2.setProdukt(testProduct2); p2.setMnozstvi(3); testCart.getPolozky().add(p2);
        PolozkaKosiku p3 = new PolozkaKosiku();
        p3.setProdukt(testProduct3); p3.setMnozstvi(1); testCart.getPolozky().add(p3);
        when(produktDao.find(1L)).thenReturn(testProduct1);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct1))).thenReturn(p1);
        cartService.removeItem(1L, 1L); testCart.getPolozky().remove(p1);
        when(produktDao.find(2L)).thenReturn(testProduct2);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct2))).thenReturn(p2);
        cartService.removeItem(1L, 2L); testCart.getPolozky().remove(p2);
        when(produktDao.find(3L)).thenReturn(testProduct3);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct3))).thenReturn(p3);
        cartService.removeItem(1L, 3L); testCart.getPolozky().remove(p3);
        assertTrue(testCart.getPolozky().isEmpty());
        verify(polozkaKosikuDao, times(3)).remove(any(PolozkaKosiku.class));
    }

    @Test
    void removeItem_NonExisting_ShouldNotThrow() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(99L)).thenReturn(testProduct1);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), any())).thenReturn(null);
        assertDoesNotThrow(() -> cartService.removeItem(1L, 99L));
        verify(polozkaKosikuDao, never()).remove(any());
    }

    @Test
    void addItem_WithNullPrice_ShouldTreatAsZero() {
        Produkt nullPriceProduct = new Produkt();
        nullPriceProduct.setProduktId(10L);
        nullPriceProduct.setCena(null);
        nullPriceProduct.setNazev("Free product");
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(10L)).thenReturn(nullPriceProduct);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(nullPriceProduct))).thenReturn(null);
        cartService.addItem(1L, 10L, 5);
        PolozkaKosiku item = new PolozkaKosiku();
        item.setProdukt(nullPriceProduct);
        item.setMnozstvi(5);
        testCart.getPolozky().add(item);
        assertEquals(0, BigDecimal.ZERO.compareTo(testCart.getCelkovaSuma()));
    }

    @Test
    void addItem_NonExistingProduct_ShouldThrow() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class,
                () -> cartService.addItem(1L, 999L, 5));
    }

    @Test
    void updateItemQuantity_NonExistingItem_ShouldThrow() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(99L)).thenReturn(testProduct1);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct1))).thenReturn(null);
        assertThrows(NoSuchElementException.class,
                () -> cartService.updateItemQuantity(1L, 99L, 10));
    }

    @Test
    void addItem_WithLargeQuantity_ShouldWork() {
        when(zakaznikDao.find(1L)).thenReturn(testCustomer);
        when(kosikDao.findByZakaznikWithItems(1L)).thenReturn(testCart);
        when(produktDao.find(1L)).thenReturn(testProduct1);
        when(polozkaKosikuDao.findByKosikAndProdukt(eq(testCart), eq(testProduct1))).thenReturn(null);
        int largeQuantity = 1000;
        Kosik result = cartService.addItem(1L, 1L, largeQuantity);
        assertNotNull(result);
        verify(polozkaKosikuDao).persist(any(PolozkaKosiku.class));
    }
}
