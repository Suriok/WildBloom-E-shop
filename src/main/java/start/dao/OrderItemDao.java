package start.dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import start.dao.exception.DaoException;
import start.model.Order;
import start.model.OrderItem;
import start.model.Product;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemDao extends BaseDao<OrderItem> {

    public OrderItemDao() { super(OrderItem.class); }

}

