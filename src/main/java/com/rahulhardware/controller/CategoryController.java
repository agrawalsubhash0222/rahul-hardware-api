package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.entity.Category;
import com.rahulhardware.entity.Product;
import com.rahulhardware.entity.SubCategory;
import com.rahulhardware.repository.CategoryRepository;
import com.rahulhardware.repository.ProductRepository;
import com.rahulhardware.repository.SubCategoryRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;

    public CategoryController(
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        category.setActive(true);

        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        return categoryRepository.save(category);
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public List<SubCategory> getSubCategories(@PathVariable String categoryId) {
        return subCategoryRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId);
    }

    @PostMapping("/subcategories")
    public SubCategory createSubCategory(@RequestBody SubCategory subCategory) {
        subCategory.setActive(true);

        if (subCategory.getDisplayOrder() == null) {
            subCategory.setDisplayOrder(0);
        }

        return subCategoryRepository.save(subCategory);
    }

    @GetMapping("/subcategories/{subCategoryId}")
    public SubCategory getSubCategoryById(@PathVariable String subCategoryId) {
        return subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new RuntimeException("Sub category not found"));
    }

    @GetMapping("/products/subcategory/{subCategoryId}")
    public List<Product> getProductsBySubCategory(@PathVariable String subCategoryId) {
        return productRepository.findBySubCategoryIdAndActiveTrue(subCategoryId);
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        product.setActive(true);
        return productRepository.save(product);
    }

    @PatchMapping("/products/{productId}/stock")
public Product addProductStock(
        @PathVariable Long productId,
        @RequestBody Map<String, Integer> body
) {
    Integer addQuantity = body.get("addQuantity");

    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

    if (addQuantity == null || addQuantity <= 0) {
        throw new RuntimeException("Add stock quantity must be greater than 0");
    }

    int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
    product.setStockQuantity(currentStock + addQuantity);

    return productRepository.save(product);
}
}