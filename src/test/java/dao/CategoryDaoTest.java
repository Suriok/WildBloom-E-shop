package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import start.dao.CategoryDao;
import start.dao.exception.DaoException;
import start.model.Category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryDaoTest {

    @Mock private EntityManager em;
    @Mock private TypedQuery<Category> typedQuery;

    @InjectMocks private CategoryDao categoryDao;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Test Category");
    }

    @Test
    void findByNameIgnoreCase_WithExistingCategory_ReturnsCategory() {
        when(em.createNamedQuery("Category.findByName", Category.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("name", "Test Category")).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(testCategory);

        Category result = categoryDao.findByNameIgnoreCase("Test Category");

        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(em, times(1)).createNamedQuery("Category.findByName", Category.class);
        verify(typedQuery, times(1)).setParameter("name", "Test Category");
        verify(typedQuery, times(1)).getSingleResult();
    }

    @Test
    void findByNameIgnoreCase_WithNonExistingCategory_ReturnsNull() {
        when(em.createNamedQuery("Category.findByName", Category.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("name", "NonExistent")).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        Category result = categoryDao.findByNameIgnoreCase("NonExistent");

        assertNull(result);
        verify(em, times(1)).createNamedQuery("Category.findByName", Category.class);
        verify(typedQuery, times(1)).setParameter("name", "NonExistent");
    }

    @Test
    void findByNameIgnoreCase_ThrowsDaoException_OnPersistenceException() {
        when(em.createNamedQuery("Category.findByName", Category.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("name", "Test")).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new PersistenceException("DB error"));

        assertThrows(DaoException.class, () -> categoryDao.findByNameIgnoreCase("Test"));
        verify(em, times(1)).createNamedQuery("Category.findByName", Category.class);
    }

    @Test
    void findByNameIgnoreCase_WithDifferentCase_StillFindsCategory() {
        when(em.createNamedQuery("Category.findByName", Category.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("name", "test category")).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(testCategory);

        Category result = categoryDao.findByNameIgnoreCase("test category");

        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(typedQuery, times(1)).setParameter("name", "test category");
    }
}

