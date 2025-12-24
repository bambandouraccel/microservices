package net.accel_tech.product_service.controllers;

import net.accel_tech.product_service.entities.Product;
import net.accel_tech.product_service.repositories.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Afficher la liste des produits
    @GetMapping("/list")
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "product-list"; // Cherchera src/main/resources/templates/product-list.html
    }

    // Afficher le formulaire d'ajout
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    // Sauvegarder un produit
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productRepository.save(product);
        return "redirect:/products/list";
    }
}