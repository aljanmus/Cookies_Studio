
package ca.sheridancollege.aljanmus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.sheridancollege.aljanmus.controllers.Cart;

public class CartTests {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    void testCartInitiallyEmpty() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getCount());
    }

    @Test
    void testAddProduct() {
        cart.add("cookie1", 3);

        assertEquals(3, cart.getItems().get("cookie1"));
        assertEquals(3, cart.getCount());
    }

    @Test
    void testAddSameProduct() {
        cart.add("cookie1", 2);
        cart.add("cookie1", 3);

        assertEquals(5, cart.getItems().get("cookie1"));
    }

    @Test
    void testMaximumQuantity() {
        cart.add("cookie1", 60);

        assertEquals(50, cart.getItems().get("cookie1"));
    }

    @Test
    void testRemoveProduct() {
        cart.add("cookie1", 5);
        cart.remove("cookie1");

        assertTrue(cart.isEmpty());
    }

    @Test
    void testUpdateQuantity() {
        cart.add("cookie1", 3);
        cart.updateQuantity("cookie1", 8);

        assertEquals(8, cart.getItems().get("cookie1"));
    }

    @Test
    void testRejectInvalidQuantity() {
        cart.add("cookie1", 3);
        cart.updateQuantity("cookie1", 0);
        cart.updateQuantity("cookie1", 51);

        assertEquals(3, cart.getItems().get("cookie1"));
    }

    @Test
    void testClearCart() {
        cart.add("cookie1", 3);
        cart.add("cookie2", 4);
        cart.clear();

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getCount());
    }

    @Test
    void testTotalCookieCount() {
        cart.add("cookie1", 3);
        cart.add("cookie2", 4);

        assertEquals(7, cart.getCount());
    }

    @Test
    void testGetItemsReturnsCopy() {
        cart.add("cookie1", 3);
        cart.getItems().clear();

        assertEquals(3, cart.getCount());
    }
}