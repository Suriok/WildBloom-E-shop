package start.dao;

import start.dao.exception.DaoException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import start.model.Category;
import start.model.Product;
import start.model.Product_;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductDao extends BaseDao<Product> {

    public ProductDao() {
        super(Product.class);
    }

    @Override
    public List<Product> findAll() {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> root = cq.from(Product.class);

            cq.where(cb.greaterThan(root.get(Product_.in_stock), 0));

            return em.createQuery(cq).getResultList();
        } catch (PersistenceException e) {
            throw new DaoException("Error finding all available products", e);
        }
    }

    public List<Product> findByCategory(Category categoryArg) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> root = cq.from(Product.class);

            cq.where(cb.equal(root.get(Product_.category), categoryArg));

            return em.createQuery(cq).getResultList();
        } catch (PersistenceException e) {
            throw new DaoException("Error finding products by category " + categoryArg, e);
        }
    }

    public List<Product> searchProducts(String nameFragment, BigDecimal minPrice, BigDecimal maxPrice) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> root = cq.from(Product.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.greaterThan(root.get(Product_.in_stock), 0));

            if (nameFragment != null && !nameFragment.trim().isEmpty()) {
                String searchStr = nameFragment.toLowerCase().trim();
                Predicate nameMatch = cb.like(cb.lower(root.get(Product_.name)), "%" + searchStr + "%");

                BigDecimal priceValue = null;
                try {
                    priceValue = new BigDecimal(searchStr);
                } catch (NumberFormatException e) {
                }

                if (priceValue != null) {
                    Predicate priceMatch = cb.equal(root.get(Product_.price), priceValue);
                    predicates.add(cb.or(nameMatch, priceMatch));
                } else {
                    predicates.add(nameMatch);
                }
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Product_.price), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(Product_.price), maxPrice));
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