package ca.sheridancollege.aljanmus.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class ProductCatalog {

    // The three cookie bases
    private static final String CLASSIC = "Classic";
    private static final String CHOCOLATE = "Chocolate";
    private static final String DIETARY = "Dietary";

    /*
     * The cookies we sell, grouped by base.
     */
    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(

        // Classic base
        new Product(
            "chocolate-chip", CLASSIC,
            "Chocolate Chip",
            "Our classic soft cookie loaded with chocolate chips.",
            new BigDecimal("2.50"),
            "chocolate.jpeg"
        ),

        new Product(
            "sugar", CLASSIC,
            "Sugar Cookie",
            "Soft and buttery, finished with a light sugar sparkle.",
            new BigDecimal("2.25"),
            null
        ),

        new Product(
            "oatmeal", CLASSIC,
            "Oatmeal",
            "Chewy oats with brown sugar and a hint of cinnamon.",
            new BigDecimal("2.50"),
            null
        ),

        new Product(
            "peanut-butter", CLASSIC,
            "Peanut Butter",
            "Rich peanut butter cookie with the classic fork-press top.",
            new BigDecimal("2.75"),
            null
        ),

        // Chocolate base
        new Product(
            "double-chocolate", CHOCOLATE,
            "Double Chocolate",
            "Rich chocolate dough with dark chocolate chunks.",
            new BigDecimal("3.00"),
            null
        ),

        new Product(
            "brownie", CHOCOLATE,
            "Brownie",
            "Crackly on top and fudgy inside, like a brownie in cookie form.",
            new BigDecimal("3.25"),
            null
        ),

        new Product(
            "fudge", CHOCOLATE,
            "Fudge",
            "Soft chocolate cookie with a gooey fudge centre.",
            new BigDecimal("3.25"),
            null
        ),

        new Product(
            "salted-caramel-chocolate", CHOCOLATE,
            "Salted Caramel Chocolate",
            "Chocolate cookie with caramel pieces and a pinch of sea salt.",
            new BigDecimal("3.50"),
            null
        ),

        // Dietary base
        new Product(
            "vegan-chocolate-chip", DIETARY,
            "Vegan Chocolate Chip",
            "Our chocolate chip cookie made with no eggs or dairy.",
            new BigDecimal("3.25"),
            null
        ),

        new Product(
            "vegan-double-chocolate", DIETARY,
            "Vegan Double Chocolate",
            "Rich double chocolate cookie made with no eggs or dairy.",
            new BigDecimal("3.50"),
            null
        ),

        new Product(
            "gluten-free-chocolate-chip", DIETARY,
            "Gluten-Free Chocolate Chip",
            "Our chocolate chip cookie made with a gluten-free flour blend.",
            new BigDecimal("3.50"),
            null
        ),

        new Product(
            "dairy-free-oatmeal", DIETARY,
            "Dairy-Free Oatmeal",
            "Chewy oatmeal cookie made without milk or butter.",
            new BigDecimal("3.25"),
            null
        )
    ));

    public List<Product> getProducts() {
        return List.copyOf(products);
    }

    public synchronized boolean updatePrice(String productId, BigDecimal price) {
        for (int index = 0; index < products.size(); index++) {
            Product product = products.get(index);
            if (product.getId().equals(productId)) {
                products.set(index, new Product(
                    product.getId(), product.getCategory(), product.getName(),
                    product.getDescription(), price, product.getImage()
                ));
                return true;
            }
        }
        return false;
    }
}
