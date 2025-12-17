//package start.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
//public class SecurityConfig {
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/",
//                                "/index.html",
//                                "/register.html",
//                                "/api/products/**",
//                                "/auth/**",
//                                "/css/**", "/js/**", "/images/**",
//                                "/h2-console/**",
//                                "/error",
//                                "/favicon.ico"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                // для H2 console
//                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
//                .formLogin(Customizer.withDefaults())
//                // удобно для Postman сценариев (можно оставить включенным)
//                .httpBasic(Customizer.withDefaults());
//
//        return http.build();
//    }
//}


package start.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index.html",
                                "/login.html", "/register.html",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/favicon.ico",
                                "/api/products/**",
                                "/auth/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/my-orders.html").hasRole("CUSTOMER")
                        .requestMatchers("/orders-management.html").hasAnyRole("EMPLOYEE", "ADMINISTRATOR")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/login.html?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            String target;
            if (roles.contains("ROLE_CUSTOMER")) {
                target = "/my-orders.html";
            } else if (roles.contains("ROLE_EMPLOYEE") || roles.contains("ROLE_ADMINISTRATOR")) {
                target = "/orders-management.html";
            } else {
                target = "/";
            }

            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
