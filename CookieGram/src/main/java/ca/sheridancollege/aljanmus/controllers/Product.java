package ca.sheridancollege.aljanmus.controllers;

import java.math.BigDecimal;

/*
 * A cookie product that customers can add to their cart.
 */
public class Product {

    private final String id;

    // The base this cookie belongs to, e.g. "Classic", "Chocolate", "Dietary"
    private final String category;

    private final String name;

    private final String description;

    private final BigDecimal price;

    // Image file name in static/images, or null if there is no image yet
    private final String image;

    public Product(
            String id,
            String category,
            String name,
            String description,
            BigDecimal price,
            String image) {

        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }
}