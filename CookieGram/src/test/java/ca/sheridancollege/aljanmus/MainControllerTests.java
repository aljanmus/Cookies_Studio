
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import ca.sheridancollege.aljanmus.controllers.MainController;

public class MainControllerTests {

    private MainController controller;

    @BeforeEach
    void setUp() {
        InMemoryUserDetailsManager userDetailsManager =
                new InMemoryUserDetailsManager();

        PasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        controller = new MainController(
                userDetailsManager,
                passwordEncoder
        );
    }

    @Test
    void testLoginPage() {
        assertEquals("login", controller.login());
    }

    @Test
    void testAdminPage() {
        Model model = new ExtendedModelMap();

        assertEquals("admin", controller.admin(model));
    }

    @Test
    void testEmployeePage() {
        assertEquals("employee", controller.employee());
    }

    @Test
    void testCustomerPage() {
        Model model = new ExtendedModelMap();

        String result = controller.customer(model);

        assertEquals("customer", result);
    }

    @Test
    void testCustomerPromotions() {
        Model model = new ExtendedModelMap();

        controller.customer(model);

        Object promotions = model.getAttribute("promotions");

        assertTrue(promotions instanceof List<?>);

        List<?> promotionList = (List<?>) promotions;

        assertEquals(3, promotionList.size());

        assertTrue(promotionList.contains(
                Map.of(
                        "title", "20% off Chocolate Chip Cookies",
                        "image", "chocolate.jpeg"
                )
        ));

        assertTrue(promotionList.contains(
                Map.of(
                        "title", "Buy 12 cookies, get 2 free",
                        "image", "dozen.jpeg"
                )
        ));

        assertTrue(promotionList.contains(
                Map.of(
                        "title", "Free delivery on orders over $30",
                        "image", "delivery.jpeg"
                )
        ));
    }
}
