package start.service;

import start.dao.AdministratorDao;
import start.dao.CustomerDao;
import start.dao.EmployeeDao;
import start.dao.UserDao;
import start.dto.CreateAdministratorDto;
import start.dto.CreateEmployeeDto;
import start.dto.UserDto;
import start.model.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserService {

    private final CustomerDao customerDao;
    private final EmployeeDao employeeDao;
    private final AdministratorDao administratorDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(CustomerDao customerDao,
                       EmployeeDao employeeDao,
                       AdministratorDao administratorDao,
                       UserDao userDao,
                       PasswordEncoder passwordEncoder) {
        this.customerDao = customerDao;
        this.employeeDao = employeeDao;
        this.administratorDao = administratorDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerCustomer(UserDto dto) {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists.");
        }

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setRole(UserRole.CUSTOMER);
        customer.setdateRegistrace(new Date());

        Cart cart = new Cart();
        cart.setCustomer(customer);
        customer.setCart(cart);

        customerDao.persist(customer);
    }

    @Transactional
    public void createEmployee(CreateEmployeeDto dto) {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists.");
        }

        Employee e = new Employee();
        e.setName(dto.getName());
        e.setEmail(dto.getEmail());
        e.setPassword(passwordEncoder.encode(dto.getPassword()));
        e.setPhone(dto.getPhone());
        e.setAddress(dto.getAddress());
        e.setRole(UserRole.EMPLOYEE);
        e.setPosition(dto.getPosition());
        e.setdateNastupu(new Date());

        employeeDao.persist(e);
    }

    @Transactional
    public void createAdministrator(CreateAdministratorDto dto) {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists.");
        }

        Administrator a = new Administrator();
        a.setName(dto.getName());
        a.setEmail(dto.getEmail());
        a.setPassword(passwordEncoder.encode(dto.getPassword()));
        a.setPhone(dto.getPhone());
        a.setAddress(dto.getAddress());
        a.setRole(UserRole.ADMINISTRATOR);
        a.setrights(dto.getRights());

        administratorDao.persist(a);
    }

    public User getByEmail(String email) {
        User u = userDao.findByEmail(email);
        if (u == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return u;
    }
}
