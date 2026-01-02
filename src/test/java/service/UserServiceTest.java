package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import start.dao.*;
import start.dto.CreateAdministratorDto;
import start.dto.CreateEmployeeDto;
import start.dto.UserDto;
import start.model.*;
import start.service.UserService;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private CustomerDao customerDao;
    @Mock private EmployeeDao employeeDao;
    @Mock private AdministratorDao administratorDao;
    @Mock private UserDao userDao;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private UserDto testUserDto;
    private CreateEmployeeDto testEmployeeDto;
    private CreateAdministratorDto testAdminDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setName("John Doe");
        testUserDto.setEmail("john@example.com");
        testUserDto.setPassword("password123");
        testUserDto.setPhone("123456789");
        testUserDto.setAddress("Test Street 123");

        testEmployeeDto = new CreateEmployeeDto();
        testEmployeeDto.setName("Jane Employee");
        testEmployeeDto.setEmail("jane@example.com");
        testEmployeeDto.setPassword("emp123");
        testEmployeeDto.setPhone("987654321");
        testEmployeeDto.setAddress("Work Street 456");
        testEmployeeDto.setPosition("Manager");

        testAdminDto = new CreateAdministratorDto();
        testAdminDto.setName("Admin User");
        testAdminDto.setEmail("admin@example.com");
        testAdminDto.setPassword("admin123");
        testAdminDto.setPhone("111222333");
        testAdminDto.setAddress("Admin Street 789");
        testAdminDto.setRights("FULL_ACCESS");
    }

    @Test
    void registerCustomer_WithValidData_CreatesCustomer() {
        when(userDao.existsByEmail(testUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUserDto.getPassword())).thenReturn("encoded_password");

        assertDoesNotThrow(() -> userService.registerCustomer(testUserDto));

        verify(userDao, times(1)).existsByEmail(testUserDto.getEmail());
        verify(passwordEncoder, times(1)).encode(testUserDto.getPassword());
        verify(customerDao, times(1)).persist(any(Customer.class));
    }

    @Test
    void registerCustomer_WithExistingEmail_ShouldThrow() {
        when(userDao.existsByEmail(testUserDto.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerCustomer(testUserDto)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerDao, never()).persist(any());
    }

    @Test
    void registerCustomer_CreatesCartForCustomer() {
        when(userDao.existsByEmail(testUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUserDto.getPassword())).thenReturn("encoded_password");

        userService.registerCustomer(testUserDto);

        verify(customerDao, times(1)).persist(argThat(customer -> {
            Customer c = (Customer) customer;
            return c.getCart() != null && 
                   c.getRole() == UserRole.CUSTOMER &&
                   c.getdateRegistrace() != null;
        }));
    }

    @Test
    void createEmployee_WithValidData_CreatesEmployee() {
        when(userDao.existsByEmail(testEmployeeDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testEmployeeDto.getPassword())).thenReturn("encoded_password");

        assertDoesNotThrow(() -> userService.createEmployee(testEmployeeDto));

        verify(userDao, times(1)).existsByEmail(testEmployeeDto.getEmail());
        verify(passwordEncoder, times(1)).encode(testEmployeeDto.getPassword());
        verify(employeeDao, times(1)).persist(any(Employee.class));
    }

    @Test
    void createEmployee_WithExistingEmail_ShouldThrow() {
        when(userDao.existsByEmail(testEmployeeDto.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createEmployee(testEmployeeDto)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(employeeDao, never()).persist(any());
    }

    @Test
    void createEmployee_SetsCorrectRoleAndPosition() {
        when(userDao.existsByEmail(testEmployeeDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testEmployeeDto.getPassword())).thenReturn("encoded_password");

        userService.createEmployee(testEmployeeDto);

        verify(employeeDao, times(1)).persist(argThat(employee -> {
            Employee e = (Employee) employee;
            return e.getRole() == UserRole.EMPLOYEE &&
                   e.getPosition().equals(testEmployeeDto.getPosition()) &&
                   e.getdateNastupu() != null;
        }));
    }

    @Test
    void createAdministrator_WithValidData_CreatesAdministrator() {
        when(userDao.existsByEmail(testAdminDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testAdminDto.getPassword())).thenReturn("encoded_password");

        assertDoesNotThrow(() -> userService.createAdministrator(testAdminDto));

        verify(userDao, times(1)).existsByEmail(testAdminDto.getEmail());
        verify(passwordEncoder, times(1)).encode(testAdminDto.getPassword());
        verify(administratorDao, times(1)).persist(any(Administrator.class));
    }

    @Test
    void createAdministrator_WithExistingEmail_ShouldThrow() {
        when(userDao.existsByEmail(testAdminDto.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAdministrator(testAdminDto)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(administratorDao, never()).persist(any());
    }

    @Test
    void createAdministrator_SetsCorrectRoleAndRights() {
        when(userDao.existsByEmail(testAdminDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testAdminDto.getPassword())).thenReturn("encoded_password");

        userService.createAdministrator(testAdminDto);

        verify(administratorDao, times(1)).persist(argThat(administrator -> {
            Administrator a = (Administrator) administrator;
            return a.getRole() == UserRole.ADMINISTRATOR &&
                   a.getrights().equals(testAdminDto.getRights());
        }));
    }

    @Test
    void getByEmail_WithExistingUser_ReturnsUser() {
        User testUser = new Customer();
        testUser.setEmail("test@example.com");
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        User result = userService.getByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userDao, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getByEmail_WithNonExistingUser_ShouldThrow() {
        when(userDao.findByEmail("nonexistent@example.com")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getByEmail("nonexistent@example.com")
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userDao, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void registerCustomer_EncodesPassword() {
        when(userDao.existsByEmail(testUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(testUserDto.getPassword())).thenReturn("encoded_password");

        userService.registerCustomer(testUserDto);

        verify(passwordEncoder, times(1)).encode(testUserDto.getPassword());
        verify(customerDao, times(1)).persist(argThat(customer -> {
            Customer c = (Customer) customer;
            return "encoded_password".equals(c.getPassword());
        }));
    }
}

