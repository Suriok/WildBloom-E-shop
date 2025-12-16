package start.dao;

import start.model.Customer;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerDao extends BaseDao<Customer> {
    public CustomerDao() { super(Customer.class); }
}

