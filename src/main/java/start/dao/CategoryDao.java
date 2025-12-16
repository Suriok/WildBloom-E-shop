package start.dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import start.dao.exception.DaoException;
import start.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryDao extends BaseDao<Category> {

    public CategoryDao() { super(Category.class); }

    public Category findByNameIgnoreCase(String name) {
        try {
            return em.createQuery(
                            "SELECT k FROM Category k WHERE LOWER(k.name) = LOWER(:name)", Category.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }catch (PersistenceException e) {
            throw new DaoException("Error finding category by name: " + name, e);
        }
    }
}


