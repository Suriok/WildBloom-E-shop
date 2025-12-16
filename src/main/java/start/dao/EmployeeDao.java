package start.dao;

import start.model.Employee;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDao extends BaseDao<Employee> {
    public EmployeeDao() { super(Employee.class); }
}

