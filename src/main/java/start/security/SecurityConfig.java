package start.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/register.html",
                                "/api/products/**",
                                "/auth/**",
                                "/css/**", "/js/**", "/images/**",
                                "/h2-console/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // для H2 console
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .formLogin(Customizer.withDefaults())
                // удобно для Postman сценариев (можно оставить включенным)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}