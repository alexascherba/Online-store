package com.scherba.store.controllers;

import com.scherba.store.models.Product;
import com.scherba.store.models.User;
import com.scherba.store.repository.UserRepository;

import com.scherba.store.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final ProductService productService;

    public UserController(UserRepository userRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/{username}")
    public String getUserPage(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username);
        List<Product> products = productService.getProductsByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("products", products);

        return "user-page";
    }
}
