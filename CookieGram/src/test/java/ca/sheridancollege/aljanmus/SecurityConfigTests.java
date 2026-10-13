
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import ca.sheridancollege.aljanmus.security.SecurityConfig;

public class SecurityConfigTests {

    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();
    UserDetailsService users = config.userDetailsService(encoder);

    @Test
    void testCustomerCredentials() {
        UserDetails customer = users.loadUserByUsername("customer");

        assertTrue(encoder.matches("Customer123!", customer.getPassword()));
        assertTrue(customer.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void testEmployeeCredentials() {
        UserDetails employee = users.loadUserByUsername("employee");

        assertTrue(encoder.matches("Employee123!", employee.getPassword()));
        assertTrue(employee.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    void testAdminCredentials() {
        UserDetails admin = users.loadUserByUsername("admin");

        assertTrue(encoder.matches("Admin123!", admin.getPassword()));
        assertTrue(admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testInvalidPassword() {
        UserDetails customer = users.loadUserByUsername("customer");

        assertFalse(encoder.matches("WrongPassword", customer.getPassword()));
    }

    @Test
    void testUnknownUsername() {
        assertThrows(
            org.springframework.security.core.userdetails.UsernameNotFoundException.class,
            () -> users.loadUserByUsername("unknown")
        );
    }

    @Test
    void testPasswordEncoding() {
        String encoded = encoder.encode("TestPassword123!");

        assertNotEquals("TestPassword123!", encoded);
        assertTrue(encoder.matches("TestPassword123!", encoded));
    }
}
