package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import start.dao.ProductDao;
import start.dao.exception.DaoException;
import start.model.Category;
import start.model.Product;
import start.model.Product_;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductDaoTest {

    @Mock private EntityManager em;
    @Mock private CriteriaBuilder criteriaBuilder;
    @Mock private CriteriaQuery<Product> criteriaQuery;
    @Mock private Root<Product> root;
    @Mock private TypedQuery<Product> typedQuery;
    @Mock private Predicate predicate;

    @InjectMocks private ProductDao productDao;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Test Category");

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setin_stock(10);
        testProduct.setCategory(testCategory);
    }

    @Test
    void findAll_ReturnsOnlyProductsWithStock() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);

        List<Product> result = productDao.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(criteriaBuilder, times(1)).greaterThan(any(), eq(0));
    }

    @Test
    void findAll_ThrowsDaoException_OnPersistenceException() {
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenThrow(new PersistenceException("DB error"));

        assertThrows(DaoException.class, () -> productDao.findAll());
    }

    @Test
    void findByCategory_ReturnsProductsInCategory() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.category)).thenReturn(mock());
        when(criteriaBuilder.equal(any(), eq(testCategory))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);
        List<Product> result = productDao.findByCategory(testCategory);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(criteriaBuilder, times(1)).equal(any(), eq(testCategory));
    }

    @Test
    void findByCategory_ThrowsDaoException_OnPersistenceException() {
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.category)).thenReturn(mock());
        when(criteriaBuilder.equal(any(), eq(testCategory))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenThrow(new PersistenceException("DB error"));

        assertThrows(DaoException.class, () -> productDao.findByCategory(testCategory));
    }

    @Test
    void searchProducts_WithNameFragment_ReturnsMatchingProducts() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(root.get(Product_.name)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaBuilder.lower(any())).thenReturn(mock());
        when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);

        List<Product> result = productDao.searchProducts("Test", null, null);

        assertNotNull(result);
        verify(criteriaBuilder, atLeastOnce()).like(any(), anyString());
    }

    @Test
    void searchProducts_WithMinPrice_AddsPricePredicate() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(root.get(Product_.price)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);

        List<Product> result = productDao.searchProducts(null, new BigDecimal("50.00"), null);

        assertNotNull(result);
        verify(criteriaBuilder, atLeastOnce()).greaterThanOrEqualTo(any(), any(BigDecimal.class));
    }

    @Test
    void searchProducts_WithMaxPrice_AddsPricePredicate() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(root.get(Product_.price)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);

        List<Product> result = productDao.searchProducts(null, null, new BigDecimal("200.00"));

        assertNotNull(result);
        verify(criteriaBuilder, atLeastOnce()).lessThanOrEqualTo(any(), any(BigDecimal.class));
    }

    @Test
    void searchProducts_WithAllParameters_CombinesPredicates() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(root.get(Product_.name)).thenReturn(mock());
        when(root.get(Product_.price)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaBuilder.lower(any())).thenReturn(mock());
        when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(products);

        List<Product> result = productDao.searchProducts("Test", 
                new BigDecimal("50.00"), 
                new BigDecimal("200.00"));

        assertNotNull(result);
        verify(criteriaBuilder, atLeastOnce()).like(any(), anyString());
        verify(criteriaBuilder, atLeastOnce()).greaterThanOrEqualTo(any(), any(BigDecimal.class));
        verify(criteriaBuilder, atLeastOnce()).lessThanOrEqualTo(any(), any(BigDecimal.class));
    }

    @Test
    void searchProducts_ThrowsDaoException_OnPersistenceException() {
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Product.class)).thenReturn(root);
        when(root.get(Product_.in_stock)).thenReturn(mock());
        when(criteriaBuilder.greaterThan(any(), eq(0))).thenReturn(predicate);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenThrow(new PersistenceException("DB error"));

        assertThrows(DaoException.class, () -> 
                productDao.searchProducts("Test", null, null));
    }
}

