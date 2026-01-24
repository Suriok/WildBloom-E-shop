package start.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import start.dao.AdministratorDao;
import start.dao.EmployeeDao;
import start.dao.UserDao;
import start.model.Administrator;
import start.model.Employee;
import start.model.UserRole;

import java.util.Date;

@Component
public class DefaultUsersInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final AdministratorDao administratorDao;
    private final EmployeeDao employeeDao;
    private final PasswordEncoder passwordEncoder;

    public DefaultUsersInitializer(UserDao userDao,
                                   AdministratorDao administratorDao,
                                   EmployeeDao employeeDao,
                                   PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.administratorDao = administratorDao;
        this.employeeDao = employeeDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userDao.existsByEmail("admin@wildbloom.cz")) {
            Administrator a = new Administrator();
            a.setName("Admin");
            a.setEmail("admin@wildbloom.cz");
            a.setPassword(passwordEncoder.encode("admin123"));
            a.setRole(UserRole.ADMINISTRATOR);
            a.setrights("FULL");
            administratorDao.persist(a);
        }

        if (!userDao.existsByEmail("employee@wildbloom.cz")) {
            Employee e = new Employee();
            e.setName("Employee");
            e.setEmail("employee@wildbloom.cz");
            e.setPassword(passwordEncoder.encode("employee123"));
            e.setRole(UserRole.EMPLOYEE);
            e.setPosition("Order Manager");
            e.setEmploymentStartDate(new Date());
            employeeDao.persist(e);
        }
    }
}
