package start.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import start.dao.UserDao;
import start.model.User;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public DbUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userDao.findByEmail(email);
        if (u == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        String role = "ROLE_" + u.getRole().name();

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities(role)
                .build();
    }
}