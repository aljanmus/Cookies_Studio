
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import ca.sheridancollege.aljanmus.controllers.Cart;
import ca.sheridancollege.aljanmus.controllers.ShopController;

class CustomerOrderTests {

    private Cart cart;
    private ShopController controller;
    private RedirectAttributesModelMap redirect;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        controller = new ShopController(cart);
        redirect = new RedirectAttributesModelMap();
    }

    @Test
    void testSuccessfulCustomerOrder() {
        cart.add("chocolate-chip", 2);

        String result = controller.placeCustomerOrder(
            "John Smith",
            "john@example.com",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals("redirect:/customer/confirmation", result);
        assertTrue(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("orderTotal"));
    }

    @Test
    void testEmptyCartCustomerOrder() {
        String result = controller.placeCustomerOrder(
            "John Smith",
            "john@example.com",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals("redirect:/cart", result);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testInvalidCustomerEmail() {
        cart.add("chocolate-chip", 2);

        String result = controller.placeCustomerOrder(
            "John Smith",
            "invalid-email",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals("redirect:/customer/payment", result);
        assertFalse(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testMissingCustomerName() {
        cart.add("chocolate-chip", 2);

        String result = controller.placeCustomerOrder(
            "",
            "john@example.com",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals("redirect:/customer/payment", result);
        assertFalse(cart.isEmpty());
    }

    @Test
    void testCustomerNameTooLong() {
        cart.add("chocolate-chip", 2);

        String result = controller.placeCustomerOrder(
            "A".repeat(101),
            "john@example.com",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals("redirect:/customer/payment", result);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testCustomerOrderTotal() {
        cart.add("chocolate-chip", 2);

        controller.placeCustomerOrder(
            "John Smith",
            "john@example.com",
            "123 Main Street",
            "",
            redirect
        );

        assertEquals(
            new java.math.BigDecimal("5.00"),
            redirect.getFlashAttributes().get("orderTotal")
        );
    }
}
