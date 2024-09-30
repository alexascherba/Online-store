package com.scherba.store.repository;

import com.scherba.store.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    void deleteById(Long id);
    List<Product> findByDescriptionContainingIgnoreCase(String description);
}

