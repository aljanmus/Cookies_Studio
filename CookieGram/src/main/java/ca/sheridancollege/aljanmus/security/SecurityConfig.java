package ca.sheridancollege.aljanmus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http

            .authorizeHttpRequests(auth -> auth

            		.requestMatchers(
            			    "/",
            			    "/login",
            			    "/contact",
            			    "/guest/**",
            			    "/css/**",
            			    "/images/**",
            			    "/error"
            			).permitAll()

                .requestMatchers("/customer/**")
                    .hasRole("CUSTOMER")

                .requestMatchers("/employee/**")
                    .hasRole("EMPLOYEE")

                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")

                .anyRequest()
                    .authenticated()
            )

            .formLogin(form -> form

                .loginPage("/login")

                .successHandler((request, response, authentication) -> {

                    String role = authentication
                            .getAuthorities()
                            .iterator()
                            .next()
                            .getAuthority();

                    String destination = switch (role) {

                        case "ROLE_ADMIN" -> "/admin";

                        case "ROLE_EMPLOYEE" -> "/employee";

                        default -> "/customer";
                    };

                    response.sendRedirect(
                        request.getContextPath() + destination
                    );
                })

                .permitAll()
            )

            .logout(logout -> logout

                .logoutSuccessUrl("/login?logout")

                .permitAll()
            )

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
            PasswordEncoder encoder) {

        return new InMemoryUserDetailsManager(

            User.withUsername("customer")
                .password(encoder.encode("Customer123!"))
                .roles("CUSTOMER")
                .build(),

            User.withUsername("employee")
                .password(encoder.encode("Employee123!"))
                .roles("EMPLOYEE")
                .build(),

            User.withUsername("admin")
                .password(encoder.encode("Admin123!"))
                .roles("ADMIN")
                .build()
        );
    }
}