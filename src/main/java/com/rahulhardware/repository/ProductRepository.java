package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findByCategoryIdAndActiveTrue(String categoryId);

    List<Product> findBySubCategoryIdAndActiveTrue(String subCategoryId);

    Optional<Product> findFirstByNameIgnoreCase(String name);
}