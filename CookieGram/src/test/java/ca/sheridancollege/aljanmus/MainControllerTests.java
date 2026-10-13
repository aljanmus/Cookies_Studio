
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import ca.sheridancollege.aljanmus.controllers.MainController;

public class MainControllerTests {

    @Test
    void testLoginPage() {
        MainController controller = new MainController();
        assertEquals("login", controller.login());
    }

    @Test
    void testAdminPage() {
        MainController controller = new MainController();
        assertEquals("admin", controller.admin());
    }

    @Test
    void testEmployeePage() {
        MainController controller = new MainController();
        assertEquals("employee", controller.employee());
    }

    @Test
    void testCustomerPage() {
        MainController controller = new MainController();
        Model model = new ExtendedModelMap();

        String result = controller.customer(model);

        assertEquals("customer", result);
    }

    @Test
    void testCustomerPromotions() {
        MainController controller = new MainController();
        Model model = new ExtendedModelMap();

        controller.customer(model);

        Object promotions = model.getAttribute("promotions");

        assertTrue(promotions instanceof List<?>);

        List<?> promotionList = (List<?>) promotions;

        assertEquals(3, promotionList.size());
        assertTrue(promotionList.contains(
            "20% off Chocolate Chip Cookies"));
        assertTrue(promotionList.contains(
            "Buy 12 cookies, get 2 free"));
        assertTrue(promotionList.contains(
            "Free delivery on orders over $30"));
    }
}
