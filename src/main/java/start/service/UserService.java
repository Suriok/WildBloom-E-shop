package start.service;

import start.dao.CustomerDao;
import start.dao.UserDao;
import start.dto.UserDto;
import start.model.Cart;
import start.model.Customer;
import start.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserService {

    private final CustomerDao customerDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(CustomerDao customerDao, UserDao userDao, PasswordEncoder passwordEncoder) {
        this.customerDao = customerDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerCustomer(UserDto dto) {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с email " + dto.getEmail() + " уже существует.");
        }

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        // password Hash
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
}