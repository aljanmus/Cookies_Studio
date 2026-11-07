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

    private final ProductCatalog productCatalog;

    public ShopController(Cart cart, ProductCatalog productCatalog) {
        this.cart = cart;
        this.productCatalog = productCatalog;
    }

    // Product list page (open to everyone)
    @GetMapping("/products")
    public String products(Model model) {

        Map<String, List<Product>> productsByCategory = productCatalog.getProducts().stream()
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

        model.addAttribute(
                "isCustomer", request.isUserInRole("CUSTOMER"));

        return "cart";
    }

    // Remove a product from the cart
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String productId) {

        cart.remove(productId);

        return "redirect:/cart";
    }

    // Update the quantity of an existing item
    @PostMapping("/cart/update")
    public String updateCartQuantity(
            @RequestParam String productId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        if (findProduct(productId) == null
                || !cart.getItems().containsKey(productId)) {

            redirectAttributes.addFlashAttribute(
                    "error", "That cookie is not in your cart.");

            return "redirect:/cart";
        }

        if (quantity < 1 || quantity > 50) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please enter a valid quantity between 1 and 50."
            );

            return "redirect:/cart";
        }

        cart.updateQuantity(productId, quantity);

        redirectAttributes.addFlashAttribute(
                "success", "Cart quantity updated successfully.");

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

        String username = principal.getName();

        model.addAttribute(
                "email", username.contains("@") ? username : "");

        return "payment";
    }

    // Place an order as a logged-in customer
    @PostMapping("/customer/payment")
    public String placeCustomerOrder(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam(defaultValue = "") String specialInstructions,
            RedirectAttributes redirectAttributes) {

        return placeOrder(
            name, email, address, specialInstructions, "Customer",
            "/customer/payment", "/customer/confirmation",
            redirectAttributes
        );
    }

    // Order confirmation for customers
    @GetMapping("/customer/confirmation")
    public String customerConfirmation() {
        return "order-confirmation";
    }

    // Checkout page for guests
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
            @RequestParam(defaultValue = "") String specialInstructions,
            RedirectAttributes redirectAttributes) {

        return placeOrder(
            name, email, address, specialInstructions, "Guest",
            "/guest/checkout", "/guest/confirmation",
            redirectAttributes
        );
    }

    // Order confirmation for guests
    @GetMapping("/guest/confirmation")
    public String guestConfirmation() {
        return "order-confirmation";
    }

    // View all customer and guest orders (Baker dashboard)
    @GetMapping("/employee/orders")
    public String viewEmployeeOrders(Model model) {

        model.addAttribute("orders", orders);

        return "employee-orders";
    }

    // Update an order's status
    @PostMapping("/employee/orders/status")
    public String updateOrderStatus(
            @RequestParam int orderIndex,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        List<String> validStatuses =
                List.of("Pending", "Preparing", "Ready", "Completed");

        if (orderIndex < 0 || orderIndex >= orders.size()) {

            redirectAttributes.addFlashAttribute(
                    "error", "Order could not be found.");

            return "redirect:/employee/orders";
        }

        if (!validStatuses.contains(status)) {

            redirectAttributes.addFlashAttribute(
                    "error", "Please select a valid order status.");

            return "redirect:/employee/orders";
        }

        orders.get(orderIndex).put("status", status);

        redirectAttributes.addFlashAttribute(
                "success", "Order status updated successfully.");

        return "redirect:/employee/orders";
    }

    /*
     * Shared order logic for customers and guests:
     * check the form, save the order, empty the cart.
     */
    private String placeOrder(
            String name,
            String email,
            String address,
            String specialInstructions,
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
        specialInstructions = specialInstructions.trim();

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
                || address.length() > 300
                || specialInstructions.length() > 500) {

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

        // Save the order with an initial status and special instructions
        Map<String, Object> order = new LinkedHashMap<>();

        order.put("type", orderType);
        order.put("name", name);
        order.put("email", email);
        order.put("address", address);
        order.put("items", itemSummary);
        order.put("total", total);
        order.put("status", "Pending");
        order.put("specialInstructions", specialInstructions);

        orders.add(order);

        cart.clear();

        redirectAttributes.addFlashAttribute("name", name);
        redirectAttributes.addFlashAttribute("orderItems", itemSummary);
        redirectAttributes.addFlashAttribute("orderTotal", total);

        return "redirect:" + confirmationUrl;
    }

    private String emptyCartRedirect(
            RedirectAttributes redirectAttributes) {

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

    // Turns the cart into cart lines with products
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

        return productCatalog.getProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst()
                .orElse(null);
    }
    

    // View the details of one customer order
    @GetMapping("/employee/orders/details")
    public String viewOrderDetails(
            @RequestParam int orderIndex,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (orderIndex < 0 || orderIndex >= orders.size()) {

            redirectAttributes.addFlashAttribute(
                    "error", "Order could not be found.");

            return "redirect:/employee/orders";
        }

        model.addAttribute("order", orders.get(orderIndex));

        return "order-details";
    }

}



