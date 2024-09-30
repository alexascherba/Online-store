package com.scherba.store.controllers;

import com.scherba.store.models.Product;
import com.scherba.store.models.ProductDto;
import com.scherba.store.models.User;
import com.scherba.store.repository.ProductRepository;
import com.scherba.store.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/", "/home"})
    public String showProducts(Model model) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result){


        if (productDto.getImageFileName().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFileName", "Image file name is required"));
        }

        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        //save image file
        MultipartFile image = productDto.getImageFileName();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "..." + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setCity(productDto.getCity());
        product.setDescription(productDto.getDescription());
        product.setImageFileName(storageFileName);
        product.setCreatedAt(createdAt);

        productRepository.save(product);


        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {

        try {
            Optional<Product> optionalProduct = productRepository.findById((long) id);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                model.addAttribute("product", product);

                ProductDto productDto = new ProductDto();
                productDto.setName(product.getName());
                productDto.setCategory(product.getCategory());
                productDto.setPrice(product.getPrice());
                productDto.setCity(product.getCity());
                productDto.setDescription(product.getDescription());

                model.addAttribute("productDto", productDto);
            } else {
                throw new Exception("Product not found");
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return "redirect:/products";
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model,
                                @RequestParam int id,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult result){

        try {
            Optional<Product> optionalProduct = productRepository.findById((long) id);
            Product product = optionalProduct.orElseThrow(() -> new Exception("Product not found"));
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if (!productDto.getImageFileName().isEmpty()) {
                //delete old img
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception" + ex.getMessage());
                }

                //save new img
                MultipartFile image = productDto.getImageFileName();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "..." + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setCity(productDto.getCity());
            product.setDescription(productDto.getDescription());
            productRepository.save(product);

        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {

        try {
            Optional<Product> optionalProduct = productRepository.findById((long) id);
            Product product = optionalProduct.orElseThrow(() -> new Exception("Product not found"));

            //delete img
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());

            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception" + ex.getMessage());
            }
            //delete product
            productRepository.delete(product);
        } catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
        }

        return "redirect:/products";
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, Authentication authentication) {
        // Получаем текущего пользователя
        User currentUser = userRepository.findByUsername(authentication.getName());

        // Получаем товар из базы данных
        Optional<Product> optionalProduct = productRepository.findById(productId);

        // Проверяем владение товаром
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (product.getUserId().equals(currentUser.getId())) {
                // Удаляем товар
                productRepository.deleteById(productId);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this product");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/search")
    public String searchProducts(@RequestParam("keywords") String keywords, Model model) {
        List<Product> searchResults = productRepository.findByDescriptionContainingIgnoreCase(keywords);
        model.addAttribute("products", searchResults);
        return "products/index";
    }

    @GetMapping("/sort")
    public String sortProducts(@RequestParam("sortBy") String sortBy, Model model) {
        List<Product> products = null;

        if (sortBy.equalsIgnoreCase("price")) {
            products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "price"));
        }  else if (sortBy.equalsIgnoreCase("city")) {
            products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "city"));
        }

        model.addAttribute("products", products);
        return "products/index";
    }
}
