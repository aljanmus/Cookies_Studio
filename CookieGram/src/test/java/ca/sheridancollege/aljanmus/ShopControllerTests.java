
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import ca.sheridancollege.aljanmus.controllers.Cart;
import ca.sheridancollege.aljanmus.controllers.ShopController;

class ShopControllerTests {

    private Cart cart;
    private ShopController controller;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        controller = new ShopController(cart);
    }

    @Test
    void testProductsPage() {
        Model model = new ExtendedModelMap();

        String result = controller.products(model);

        assertEquals("products", result);
        assertNotNull(model.getAttribute("productsByCategory"));
        assertEquals(0, model.getAttribute("cartCount"));
    }

    @Test
    void testAddProductToCart() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.addToCart(
                "chocolate-chip", 2, redirect);

        assertEquals("redirect:/products", result);
        assertEquals(2, cart.getCount());
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testRejectInvalidProduct() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.addToCart(
                "invalid-cookie", 2, redirect);

        assertEquals("redirect:/products", result);
        assertTrue(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testRejectInvalidQuantity() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        controller.addToCart("chocolate-chip", 21, redirect);

        assertTrue(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testRemoveProductFromCart() {
        cart.add("chocolate-chip", 3);

        String result =
                controller.removeFromCart("chocolate-chip");

        assertEquals("redirect:/cart", result);
        assertTrue(cart.isEmpty());
    }

    @Test
    void testUpdateCartQuantity() {
        cart.add("chocolate-chip", 2);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.updateCartQuantity(
                "chocolate-chip", 5, redirect);

        assertEquals("redirect:/cart", result);
        assertEquals(5, cart.getCount());
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testRejectUpdateForMissingProduct() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        controller.updateCartQuantity(
                "chocolate-chip", 5, redirect);

        assertTrue(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testEmptyGuestCheckout() {
        Model model = new ExtendedModelMap();

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result =
                controller.guestCheckout(model, redirect);

        assertEquals("redirect:/cart", result);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testGuestConfirmationPage() {
        assertEquals("order-confirmation",
                controller.guestConfirmation());
    }

    @Test
    void testCustomerConfirmationPage() {
        assertEquals("order-confirmation",
                controller.customerConfirmation());
    }

    @Test
    void testPlaceGuestOrder() {
        cart.add("chocolate-chip", 2);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.placeGuestOrder(
                "John Smith",
                "john@example.com",
                "123 Main Street",
                "No special instructions",
                redirect);

        assertEquals("redirect:/guest/confirmation", result);
        assertTrue(cart.isEmpty());
        assertEquals("John Smith",
                redirect.getFlashAttributes().get("name"));
        assertEquals(new BigDecimal("5.00"),
                redirect.getFlashAttributes().get("orderTotal"));
    }

    @Test
    void testRejectInvalidGuestEmail() {
        cart.add("chocolate-chip", 2);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.placeGuestOrder(
                "John Smith",
                "invalid-email",
                "123 Main Street",
                "",
                redirect);

        assertEquals("redirect:/guest/checkout", result);
        assertEquals(2, cart.getCount());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testRejectMissingGuestName() {
        cart.add("chocolate-chip", 1);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String result = controller.placeGuestOrder(
                "",
                "john@example.com",
                "123 Main Street",
                "",
                redirect);

        assertEquals("redirect:/guest/checkout", result);
        assertFalse(cart.isEmpty());
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testGuestOrderAppearsForEmployee() {
        cart.add("sugar", 2);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        controller.placeGuestOrder(
                "John Smith",
                "john@example.com",
                "123 Main Street",
                "",
                redirect);

        Model model = new ExtendedModelMap();

        String result = controller.viewEmployeeOrders(model);

        assertEquals("employee-orders", result);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders =
                (List<Map<String, Object>>)
                model.getAttribute("orders");

        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals("Guest", orders.get(0).get("type"));
        assertEquals("Pending", orders.get(0).get("status"));
    }
}
