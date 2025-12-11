package dao;
import dao.exception.DaoException;
import jakarta.persistence.PersistenceException;
import model.Category;
import model.Product;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDao extends BaseDao<Product> {

    public ProductDao() { super(Product.class); }

    public List<Product> findByCategory(Category category) {
        try {
            return em.createQuery("SELECT p FROM Product p WHERE p.category = :k", Product.class)
                    .setParameter("k", category)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new DaoException("Error finding products by category " + category, e);
        }
    }
}
