package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySubCategoryIdAndActiveTrue(String subCategoryId);

    List<Product> findByActiveTrue();

    Optional<Product> findFirstByNameIgnoreCase(String name);
}