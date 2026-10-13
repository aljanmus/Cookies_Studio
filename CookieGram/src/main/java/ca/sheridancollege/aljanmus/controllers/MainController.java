package ca.sheridancollege.aljanmus.controllers;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    // customer
    @GetMapping("/customer")
    public String customer(Model model) {
        model.addAttribute("promotions", List.of(
            Map.of("title", "20% off Chocolate Chip Cookies", "image", "chocolate.jpeg"),
            Map.of("title", "Buy 12 cookies, get 2 free",     "image", "dozen.jpeg"),
            Map.of("title", "Free delivery on orders over $30", "image", "delivery.jpeg")
        ));
        return "customer";
    }
    @GetMapping("/employee")
    public String employee() {
        return "employee";
    }
}

