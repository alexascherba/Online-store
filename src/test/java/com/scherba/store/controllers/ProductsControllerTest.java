package com.scherba.store.controllers;

import com.scherba.store.models.Product;
import com.scherba.store.models.ProductDto;
import com.scherba.store.repository.ProductRepository;
import com.scherba.store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductsControllerTest {
    @InjectMocks
    private ProductsController productsController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void showCreatePage() {
        Model model = mock(Model.class);

        String viewName = productsController.showCreatePage(model);

        assertEquals("products/CreateProduct", viewName);
        verify(model, times(1)).addAttribute(eq("productDto"), any(ProductDto.class));
    }

    @Test
    void createProduct_withValidProductDto() throws Exception {
        ProductDto productDto = new ProductDto();
        MultipartFile mockImage = mock(MultipartFile.class);
        productDto.setImageFileName(mockImage);
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);
        when(mockImage.isEmpty()).thenReturn(false);
        Product product = new Product();
        when(productRepository.save(any(Product.class))).thenReturn(product);

        String viewName = productsController.createProduct(productDto, mockBindingResult);

        assertEquals("redirect:/products", viewName);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void showEditPage_withExistingProduct() throws Exception {
        Model model = mock(Model.class);
        int productId = 1;
        Product product = new Product();
        Optional<Product> optionalProduct = Optional.of(product);
        when(productRepository.findById(anyLong())).thenReturn(optionalProduct);

        String viewName = productsController.showEditPage(model, productId);

        assertEquals("products/EditProduct", viewName);
        verify(model, times(1)).addAttribute(eq("product"), eq(product));
        verify(model, times(1)).addAttribute(eq("productDto"), any(ProductDto.class));
    }

    @Test
    void showEditPage_withNonExistingProduct() throws Exception {
        Model model = mock(Model.class);
        int productId = 1;
        Optional<Product> optionalProduct = Optional.empty();
        when(productRepository.findById(anyLong())).thenReturn(optionalProduct);

        String viewName = productsController.showEditPage(model, productId);

        assertEquals("redirect:/products", viewName);
        verify(model, never()).addAttribute(eq("product"), any(Product.class));
        verify(model, never()).addAttribute(eq("productDto"), any(ProductDto.class));
    }

    @Test
    void deleteProduct_withExistingProduct() throws Exception {
        int productId = 1;
        Product product = new Product();
        Optional<Product> optionalProduct = Optional.of(product);
        when(productRepository.findById(anyLong())).thenReturn(optionalProduct);

        String viewName = productsController.deleteProduct(productId);

        assertEquals("redirect:/products", viewName);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    void deleteProduct_withNonExistingProduct() throws Exception {
        int productId = 1;
        Optional<Product> optionalProduct = Optional.empty();
        when(productRepository.findById(anyLong())).thenReturn(optionalProduct);

        String viewName = productsController.deleteProduct(productId);

        assertEquals("redirect:/products", viewName);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void searchProducts() {
        String keywords = "search";
        Model model = mock(Model.class);
        List<Product> searchResults = Arrays.asList(new Product(), new Product());
        when(productRepository.findByDescriptionContainingIgnoreCase(keywords)).thenReturn(searchResults);

        String viewName = productsController.searchProducts(keywords, model);

        assertEquals("products/index", viewName);
        verify(model, times(1)).addAttribute("products", searchResults);
    }

}