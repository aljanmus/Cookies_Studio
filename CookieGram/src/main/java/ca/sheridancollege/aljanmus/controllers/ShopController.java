package ca.sheridancollege.aljanmus.controllers;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/*
 * Products, cart and checkout (for customers and guests).
 */
@Controller
public class ShopController {

    private static final String EMAIL_PATTERN =
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    // The visitor's cart (one per session)
    private final Cart cart;

    /*
     * Stores all placed orders (customers and guests).
     * This list resets when the application restarts.
     */
    private final List<Map<String, Object>> orders =
            new CopyOnWriteArrayList<>();

    // The three cookie bases, in the order they appear on the products page
    private static final String CLASSIC = "Classic";
    private static final String CHOCOLATE = "Chocolate";
    private static final String DIETARY = "Dietary";

    /*
     * The cookies we sell, grouped by base. Kept in memory for now.
     * Products without an image show a cookie icon instead.
     */
    private final List<Product> products = List.of(

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
    );

    public ShopController(Cart cart) {

        this.cart = cart;
    }

    // Product list page (open to everyone)
    @GetMapping("/products")
    public String products(Model model) {

        // Group the cookies by base, keeping the order above:
        // Classic -> [...], Chocolate -> [...], Dietary -> [...]
        Map<String, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(
                    Product::getCategory,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

        model.addAttribute("productsByCategory", productsByCategory);

        model.addAttribute("cartCount", cart.getCount());

        return "products";
    }

    // Add a product to the cart
    @PostMapping("/cart/add")
    public String addToCart(

            @RequestParam String productId,

            @RequestParam(defaultValue = "1") int quantity,

            RedirectAttributes redirectAttributes) {

        Product product = findProduct(productId);

        // Check the product exists and the quantity is reasonable
        if (product == null || quantity < 1 || quantity > 20) {

            redirectAttributes.addFlashAttribute(
                "error",
                "Please choose a valid cookie and a quantity between 1 and 20."
            );

            return "redirect:/products";
        }

        cart.add(productId, quantity);

        redirectAttributes.addFlashAttribute(
            "success",
            quantity + " × " + product.getName() + " added to your cart."
        );

        return "redirect:/products";
    }

    // Cart page (open to everyone)
    @GetMapping("/cart")
    public String viewCart(Model model, HttpServletRequest request) {

        addCartDetails(model);

        // Logged-in customers go straight to checkout;
        // everyone else chooses to log in or check out as a guest
        model.addAttribute("isCustomer", request.isUserInRole("CUSTOMER"));

        return "cart";
    }

    // Remove a product from the cart
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String productId) {

        cart.remove(productId);

        return "redirect:/cart";
    }

    // Checkout page for logged-in customers
    @GetMapping("/customer/payment")
    public String customerCheckout(

            Model model,

            Principal principal,

            RedirectAttributes redirectAttributes) {

        if (cart.isEmpty()) {

            return emptyCartRedirect(redirectAttributes);
        }

        addCartDetails(model);

        // Customers who registered use their email as their username,
        // so we can fill in the email field for them
        String username = principal.getName();

        model.addAttribute("email", username.contains("@") ? username : "");

        return "payment";
    }

    // Place an order as a logged-in customer
    @PostMapping("/customer/payment")
    public String placeCustomerOrder(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String address,

            RedirectAttributes redirectAttributes) {

        return placeOrder(
            name, email, address, "Customer",
            "/customer/payment", "/customer/confirmation",
            redirectAttributes
        );
    }

    // Order confirmation for customers
    @GetMapping("/customer/confirmation")
    public String customerConfirmation() {

        return "order-confirmation";
    }

    // Checkout page for guests (no login needed)
    @GetMapping("/guest/checkout")
    public String guestCheckout(

            Model model,

            RedirectAttributes redirectAttributes) {

        if (cart.isEmpty()) {

            return emptyCartRedirect(redirectAttributes);
        }

        addCartDetails(model);

        return "guest-checkout";
    }

    // Place an order as a guest
    @PostMapping("/guest/checkout")
    public String placeGuestOrder(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String address,

            RedirectAttributes redirectAttributes) {

        return placeOrder(
            name, email, address, "Guest",
            "/guest/checkout", "/guest/confirmation",
            redirectAttributes
        );
    }

    // Order confirmation for guests
    @GetMapping("/guest/confirmation")
    public String guestConfirmation() {

        return "order-confirmation";
    }

    /*
     * Shared order logic for customers and guests:
     * check the form, save the order, empty the cart.
     */
    private String placeOrder(

            String name,

            String email,

            String address,

            String orderType,

            String formUrl,

            String confirmationUrl,

            RedirectAttributes redirectAttributes) {

        // The cart may be empty if the form was submitted twice
        if (cart.isEmpty()) {

            return emptyCartRedirect(redirectAttributes);
        }

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        address = address.trim();

        // Check required fields and email format
        if (name.isBlank()
                || email.isBlank()
                || address.isBlank()
                || !email.matches(EMAIL_PATTERN)) {

            redirectAttributes.addFlashAttribute(
                "error",
                "Please enter your name, a valid email and your address."
            );

            return "redirect:" + formUrl;
        }

        // Check field lengths
        if (name.length() > 100
                || email.length() > 150
                || address.length() > 300) {

            redirectAttributes.addFlashAttribute(
                "error",
                "One of the fields is too long. Please check your information."
            );

            return "redirect:" + formUrl;
        }

        List<CartItem> items = getCartItems();

        BigDecimal total = getTotal(items);

        // Example: "2 × Chocolate Chip, 1 × Sugar Cookie"
        String itemSummary = items.stream()
                .map(item -> item.getQuantity() + " × "
                        + item.getProduct().getName())
                .collect(Collectors.joining(", "));

        // Save the order
        orders.add(

            Map.of(
                "type", orderType,
                "name", name,
                "email", email,
                "address", address,
                "items", itemSummary,
                "total", total
            )
        );

        cart.clear();

        redirectAttributes.addFlashAttribute("name", name);

        redirectAttributes.addFlashAttribute("orderItems", itemSummary);

        redirectAttributes.addFlashAttribute("orderTotal", total);

        return "redirect:" + confirmationUrl;
    }

    private String emptyCartRedirect(RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
            "error",
            "Your cart is empty. Please add some cookies first."
        );

        return "redirect:/cart";
    }

    // Adds the cart lines, total and count to the page
    private void addCartDetails(Model model) {

        List<CartItem> items = getCartItems();

        model.addAttribute("cartItems", items);

        model.addAttribute("cartTotal", getTotal(items));

        model.addAttribute("cartCount", cart.getCount());
    }

    // Turns the cart (productId -> quantity) into cart lines with products
    private List<CartItem> getCartItems() {

        List<CartItem> items = new ArrayList<>();

        cart.getItems().forEach((productId, quantity) -> {

            Product product = findProduct(productId);

            if (product != null) {
                items.add(new CartItem(product, quantity));
            }
        });

        return items;
    }

    private BigDecimal getTotal(List<CartItem> items) {

        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Product findProduct(String productId) {

        return products.stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst()
                .orElse(null);
    }
}