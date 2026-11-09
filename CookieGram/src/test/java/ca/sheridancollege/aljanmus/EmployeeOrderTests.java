
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import ca.sheridancollege.aljanmus.controllers.Cart;
import ca.sheridancollege.aljanmus.controllers.ShopController;

class EmployeeOrderTests {

    private Cart cart;
    private ShopController controller;
    private RedirectAttributesModelMap redirect;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        controller = new ShopController(cart);
        redirect = new RedirectAttributesModelMap();
    }

    // Helper: create an order for employee testing
    private void createOrder() {
        cart.add("chocolate-chip", 2);

        controller.placeGuestOrder(
            "John Smith",
            "john@example.com",
            "123 Main Street",
            "",
            new RedirectAttributesModelMap()
        );
    }

    @Test
    void testEmployeeOrdersPage() {
        Model model = new ExtendedModelMap();

        String result = controller.viewEmployeeOrders(model);

        assertEquals("employee-orders", result);
        assertNotNull(model.getAttribute("orders"));
    }

    @Test
    void testEmployeeCanSeeGuestOrder() {
        createOrder();

        Model model = new ExtendedModelMap();
        controller.viewEmployeeOrders(model);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders =
            (List<Map<String, Object>>) model.getAttribute("orders");

        assertEquals(1, orders.size());
        assertEquals("Guest", orders.get(0).get("type"));
        assertEquals("Pending", orders.get(0).get("status"));
    }

    @Test
    void testUpdateOrderToPreparing() {
        createOrder();

        String result = controller.updateOrderStatus(
            0, "Preparing", redirect
        );

        assertEquals("redirect:/employee/orders", result);
        assertEquals("Preparing", getFirstOrder().get("status"));
        assertNotNull(
            redirect.getFlashAttributes().get("success")
        );
    }

    @Test
    void testUpdateOrderToReady() {
        createOrder();

        controller.updateOrderStatus(0, "Ready", redirect);

        assertEquals("Ready", getFirstOrder().get("status"));
    }

    @Test
    void testUpdateOrderToCompleted() {
        createOrder();

        controller.updateOrderStatus(0, "Completed", redirect);

        assertEquals("Completed", getFirstOrder().get("status"));
    }

    @Test
    void testRejectInvalidOrderStatus() {
        createOrder();

        String result = controller.updateOrderStatus(
            0, "Cancelled", redirect
        );

        assertEquals("redirect:/employee/orders", result);
        assertEquals("Pending", getFirstOrder().get("status"));
        assertNotNull(
            redirect.getFlashAttributes().get("error")
        );
    }

    @Test
    void testRejectNegativeOrderIndex() {
        createOrder();

        String result = controller.updateOrderStatus(
            -1, "Ready", redirect
        );

        assertEquals("redirect:/employee/orders", result);
        assertEquals("Pending", getFirstOrder().get("status"));
        assertNotNull(
            redirect.getFlashAttributes().get("error")
        );
    }

    @Test
    void testRejectMissingOrder() {
        String result = controller.updateOrderStatus(
            0, "Ready", redirect
        );

        assertEquals("redirect:/employee/orders", result);
        assertNotNull(
            redirect.getFlashAttributes().get("error")
        );
    }

    @Test
    void testViewOrderDetails() {
        createOrder();

        Model model = new ExtendedModelMap();

        String result = controller.viewOrderDetails(
            0, model, redirect
        );

        assertEquals("order-details", result);
        assertNotNull(model.getAttribute("order"));

        @SuppressWarnings("unchecked")
        Map<String, Object> order =
            (Map<String, Object>) model.getAttribute("order");

        assertEquals("John Smith", order.get("name"));
        assertEquals("Pending", order.get("status"));
    }

    @Test
    void testRejectInvalidOrderDetails() {
        Model model = new ExtendedModelMap();

        String result = controller.viewOrderDetails(
            99, model, redirect
        );

        assertEquals("redirect:/employee/orders", result);
        assertNotNull(
            redirect.getFlashAttributes().get("error")
        );
    }

    // Helper: retrieve the first saved order
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFirstOrder() {
        Model model = new ExtendedModelMap();
        controller.viewEmployeeOrders(model);

        List<Map<String, Object>> orders =
            (List<Map<String, Object>>) model.getAttribute("orders");

        return orders.get(0);
    }
}
