package ca.sheridancollege.aljanmus.controllers;


import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/*
 * The shopping cart.
 *
 * @SessionScope gives every visitor (guest or customer) their own cart.
 * It is kept in memory and is cleared when the session ends,
 * when an order is placed, or when the application restarts.
 */
@Component
@SessionScope
public class Cart {

    // Highest quantity allowed for one product
    private static final int MAX_PER_PRODUCT = 50;

    // productId -> quantity (keeps the order items were added)
    private final Map<String, Integer> items = new LinkedHashMap<>();

    // Add a product, or increase its quantity if it is already in the cart
    public synchronized void add(String productId, int quantity) {

        int newQuantity = items.getOrDefault(productId, 0) + quantity;

        items.put(productId, Math.min(newQuantity, MAX_PER_PRODUCT));
    }

    public synchronized void remove(String productId) {

        items.remove(productId);
    }

    public synchronized void clear() {

        items.clear();
    }

    // Returns a copy so other classes cannot change the cart directly
    public synchronized Map<String, Integer> getItems() {

        return new LinkedHashMap<>(items);
    }

    public synchronized boolean isEmpty() {

        return items.isEmpty();
    }

    // Total number of cookies in the cart
    public synchronized int getCount() {

        return items.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}