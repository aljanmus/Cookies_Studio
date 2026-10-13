package ca.sheridancollege.aljanmus.controllers;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;

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
            "20% off Chocolate Chip Cookies",
            "Buy 12 cookies, get 2 free",
            "Free delivery on orders over $30"
        ));
        return "customer";
    }
    
    @GetMapping("/employee")
    public String employee() {
        return "employee";
    }
}

