package net.accel_tech.product_service.controllers;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.accel_tech.product_service.dto.CategoryDTO;
import net.accel_tech.product_service.entities.Product;
import net.accel_tech.product_service.message.Message;
import net.accel_tech.product_service.repositories.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    public ProductController(ProductRepository productRepository, RestTemplate restTemplate) {
        this.productRepository = productRepository;
        this.restTemplate = restTemplate;
    }

    // Afficher la liste des produits
    @GetMapping("/list")
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();

        // Cet appel est maintenant protégé
        List<CategoryDTO> categories = fetchCategories();

        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CategoryDTO::getId,
                        CategoryDTO::getName, (a, b) -> a));

        model.addAttribute("products", products);
        model.addAttribute("categoryNames", categoryMap);
        return "product-list";
    }

    // Méthode utilitaire pour récupérer les catégories via l'API Gateway ou Eureka
    // 1. On définit le Circuit Breaker ici
    @CircuitBreaker(name = "categoryServiceCB", fallbackMethod = "fallbackFetchCategories")
    public List<CategoryDTO> fetchCategories() {
        CategoryDTO[] categories = restTemplate.getForObject("http://category-service/api/categories", CategoryDTO[].class);
        return categories != null ? Arrays.asList(categories) : Collections.emptyList();
    }

    // 2. Méthode de secours : si Category-Service est down, on ne plante pas !
    public List<?> fallbackFetchCategories(Throwable t) {
        System.err.println("Circuit Breaker activé ! Raison : " + t.getMessage());
        // On retourne une liste vide ou une catégorie factice pour éviter l'erreur 500
        return Collections.singletonList(new Message("Service Catégorie Indisponible"));
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("allCategories", fetchCategories()); // On envoie la liste au HTML
        return "add-product";
    }

    // Sauvegarder un produit
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productRepository.save(product);
        return "redirect:/products/list";
    }

    @GetMapping("/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + id));

        model.addAttribute("product", product);
        return "product-detail";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));
        model.addAttribute("product", product);
        model.addAttribute("allCategories", fetchCategories());
        return "edit-product";
    }

    // 2. Traiter la mise à jour
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, @ModelAttribute("product") Product product) {
        // On s'assure que l'ID est bien celui du produit à modifier
        product.setId(id);
        // Hibernate détectera que l'ID existe et fera un UPDATE au lieu d'un INSERT
        productRepository.save(product);
        return "redirect:/products/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productRepository.deleteById(id);
            // Ajout d'un message flash qui s'affichera une seule fois après la redirection
            redirectAttributes.addFlashAttribute("message", "Le produit a été supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression du produit.");
        }
        return "redirect:/products/list";
    }

}