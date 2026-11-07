package ca.sheridancollege.aljanmus.controllers;

import java.math.BigDecimal;

/*
 * One line in the cart: a product and how many the visitor wants.
 */
public class CartItem {

    private final Product product;

    private final int quantity;

    public CartItem(Product product, int quantity) {

        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    // Price x quantity
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}