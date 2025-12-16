package start.dao;
import start.dao.exception.DaoException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.*;
import start.model.Category;
import start.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    public List<Product> searchProducts(String nameFragment, BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> root = cq.from(Product.class);

            List<Predicate> predicates = new ArrayList<>();

            if (nameFragment != null && !nameFragment.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameFragment.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), maxPrice));
            }

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }

            return em.createQuery(cq).getResultList();

        } catch (PersistenceException e) {
            throw new DaoException("Error searching products with criteria API", e);
        }
    }
}
