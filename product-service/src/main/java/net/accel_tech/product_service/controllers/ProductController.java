package net.accel_tech.product_service.controllers;

import net.accel_tech.product_service.dto.CategoryDTO;
import net.accel_tech.product_service.entities.Product;
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
        List<CategoryDTO> categories = fetchCategories();

        // Création d'une Map pour associer ID -> Nom rapidement
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CategoryDTO::getId, CategoryDTO::getName));

        model.addAttribute("products", products);
        model.addAttribute("categoryNames", categoryMap);
        return "product-list";
    }

    // Méthode utilitaire pour récupérer les catégories via l'API Gateway ou Eureka
    private List<CategoryDTO> fetchCategories() {
        // On utilise le nom du service enregistré dans Eureka
        CategoryDTO[] categories = restTemplate.getForObject("http://category-service/api/categories", CategoryDTO[].class);
        return categories != null ? Arrays.asList(categories) : Collections.emptyList();
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