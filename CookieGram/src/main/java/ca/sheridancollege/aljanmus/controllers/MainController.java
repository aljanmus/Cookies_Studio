package ca.sheridancollege.aljanmus.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private final InMemoryUserDetailsManager userDetailsManager;

    private final PasswordEncoder passwordEncoder;

    /*
     * Stores the employees created by the owner.
     * This list resets when the application restarts.
     */
    private final List<Map<String, String>> employees =
            new CopyOnWriteArrayList<>();

    private final List<Map<String, String>> contactMessages =
            new CopyOnWriteArrayList<>();

    /*
     * Stores orders placed by guests (no account).
     * This list resets when the application restarts.
     */
    private final List<Map<String, String>> guestOrders =
            new CopyOnWriteArrayList<>();

    public MainController(
            InMemoryUserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder) {

        this.userDetailsManager = userDetailsManager;

        this.passwordEncoder = passwordEncoder;
    }

    // Home page
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("promotions", getPromotions());

        return "home";
    }

    // Login page
    @GetMapping("/login")
    public String login() {

        return "login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {

        model.addAttribute("employees", employees);

        model.addAttribute("contactMessages", contactMessages);

        return "admin";
    }

    // Open the add employee page
    @GetMapping("/admin/add-employee")
    public String addEmployee() {

        return "add-employee";
    }

    // Create a new employee account
    @PostMapping("/admin/add-employee")
    public synchronized String createEmployee(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String password,

            RedirectAttributes redirectAttributes) {

        name = name.trim();

        email = email.trim().toLowerCase(Locale.ROOT);

        // Check required fields and email format
        if (name.isBlank()
                || email.isBlank()
                || password.isBlank()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            redirectAttributes.addFlashAttribute(
                "error",
                "Please enter a name, a valid email and a password."
            );

            return "redirect:/admin/add-employee";
        }

        // Check if the email already exists
        if (userDetailsManager.userExists(email)) {

            redirectAttributes.addFlashAttribute(
                "error",
                "An account with this email already exists."
            );

            return "redirect:/admin/add-employee";
        }

        // Create the employee login account
        userDetailsManager.createUser(

            User.withUsername(email)

                .password(passwordEncoder.encode(password))

                .roles("EMPLOYEE")

                .build()
        );

        // Add employee information to the dashboard list
        employees.add(

            Map.of(
                "name", name,
                "email", email,
                "role", "Employee"
            )
        );

        redirectAttributes.addFlashAttribute(
            "success",
            "Employee account created successfully."
        );

        return "redirect:/admin";
    }

    // Customer page
    @GetMapping("/customer")
    public String customer(Model model) {

        model.addAttribute("promotions", getPromotions());

        return "customer";
    }

    // Payment page for logged-in customers
    @GetMapping("/customer/payment")
    public String payment() {

        return "payment";
    }

    // Open the guest checkout page (no login needed)
    @GetMapping("/guest/checkout")
    public String guestCheckout() {

        return "guest-checkout";
    }

    // Receive a guest order
    @PostMapping("/guest/checkout")
    public String placeGuestOrder(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String address,

            RedirectAttributes redirectAttributes) {

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        address = address.trim();

        // Check required fields and email format
        if (name.isBlank()
                || email.isBlank()
                || address.isBlank()
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            redirectAttributes.addFlashAttribute(
                "error",
                "Please enter your name, a valid email and your address."
            );

            return "redirect:/guest/checkout";
        }

        // Check field lengths
        if (name.length() > 100
                || email.length() > 150
                || address.length() > 300) {

            redirectAttributes.addFlashAttribute(
                "error",
                "One of the fields is too long. Please check your information."
            );

            return "redirect:/guest/checkout";
        }

        // Save the guest order
        guestOrders.add(

            Map.of(
                "name", name,
                "email", email,
                "address", address
            )
        );

        redirectAttributes.addFlashAttribute("name", name);

        return "redirect:/guest/confirmation";
    }

    // Show the order confirmation to the guest
    @GetMapping("/guest/confirmation")
    public String guestConfirmation() {

        return "guest-confirmation";
    }

    // Employee page
    @GetMapping("/employee")
    public String employee() {

        return "employee";
    }

    // Open the Contact Us page
    @GetMapping("/contact")
    public String contact() {

        return "contact";
    }

    // Receive a message from the Contact Us page
    @PostMapping("/contact")
    public String sendContactMessage(

            @RequestParam String name,

            @RequestParam(required = false, defaultValue = "") String email,

            @RequestParam(required = false, defaultValue = "") String phone,

            @RequestParam String message,

            RedirectAttributes redirectAttributes) {

        name = name.trim();
        email = email.trim();
        phone = phone.trim();
        message = message.trim();

        if (name.isEmpty() || message.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                "error",
                "Please enter your name and message."
            );

            return "redirect:/contact";
        }

        if (name.length() > 100
                || email.length() > 150
                || phone.length() > 30
                || message.length() > 2000) {

            redirectAttributes.addFlashAttribute(
                "error",
                "One of the fields is too long. Please check your message."
            );

            return "redirect:/contact";
        }

        contactMessages.add(

            Map.of(
                "name", name,
                "email", email,
                "phone", phone,
                "message", message
            )
        );

        redirectAttributes.addFlashAttribute(
            "success",
            "Your message has been sent. Thank you for contacting us!"
        );

        return "redirect:/contact";
    }

    private List<Map<String, String>> getPromotions() {

        return List.of(

            Map.of(
                "title", "20% off Chocolate Chip Cookies",
                "image", "chocolate.jpeg"
            ),

            Map.of(
                "title", "Buy 12 cookies, get 2 free",
                "image", "dozen.jpeg"
            ),

            Map.of(
                "title", "Free delivery on orders over $30",
                "image", "delivery.jpeg"
            )
        );
    }
}