package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

        if (category.getImageUrl() == null) {
            category.setImageUrl("");
        }

        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        return categoryRepository.save(category);
    }

    @PutMapping("/categories/{categoryId}")
    public Category updateCategory(
            @PathVariable String categoryId,
            @RequestBody Category request
    ) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setName(request.getName().trim());
        }

        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        return categoryRepository.save(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    @Transactional
    public Map<String, Object> deleteCategory(@PathVariable String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setActive(false);
        categoryRepository.save(category);

        List<SubCategory> subCategories =
                subCategoryRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId);

        for (SubCategory subCategory : subCategories) {
            subCategory.setActive(false);
        }

        subCategoryRepository.saveAll(subCategories);

        List<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId);

        for (Product product : products) {
            product.setActive(false);
        }

        productRepository.saveAll(products);

        return Map.of(
                "success", true,
                "message", "Category deleted successfully",
                "categoryId", categoryId,
                "deactivatedSubCategories", subCategories.size(),
                "deactivatedProducts", products.size()
        );
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public List<SubCategory> getSubCategories(@PathVariable String categoryId) {
        return subCategoryRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId);
    }

    @PostMapping("/subcategories")
    public SubCategory createSubCategory(@RequestBody SubCategory subCategory) {
        subCategory.setActive(true);

        if (subCategory.getImageUrl() == null) {
            subCategory.setImageUrl("");
        }

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

    @PutMapping("/subcategories/{subCategoryId}")
    public SubCategory updateSubCategory(
            @PathVariable String subCategoryId,
            @RequestBody SubCategory request
    ) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new RuntimeException("Sub category not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            subCategory.setName(request.getName().trim());
        }

        if (request.getImageUrl() != null) {
            subCategory.setImageUrl(request.getImageUrl());
        }

        if (request.getCategoryId() != null && !request.getCategoryId().trim().isEmpty()) {
            subCategory.setCategoryId(request.getCategoryId());
        }

        if (request.getDisplayOrder() != null) {
            subCategory.setDisplayOrder(request.getDisplayOrder());
        }

        return subCategoryRepository.save(subCategory);
    }

    @DeleteMapping("/subcategories/{subCategoryId}")
    @Transactional
    public Map<String, Object> deleteSubCategory(@PathVariable String subCategoryId) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new RuntimeException("Sub category not found"));

        subCategory.setActive(false);
        subCategoryRepository.save(subCategory);

        List<Product> products = productRepository.findBySubCategoryIdAndActiveTrue(subCategoryId);

        for (Product product : products) {
            product.setActive(false);
        }

        productRepository.saveAll(products);

        return Map.of(
                "success", true,
                "message", "Sub category deleted successfully",
                "subCategoryId", subCategoryId,
                "deactivatedProducts", products.size()
        );
    }

    @GetMapping("/products/subcategory/{subCategoryId}")
    public List<Product> getProductsBySubCategory(@PathVariable String subCategoryId) {
        return productRepository.findBySubCategoryIdAndActiveTrue(subCategoryId);
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    @GetMapping("/products/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        product.setActive(true);
        return productRepository.save(product);
    }

    @PutMapping("/products/{productId}")
    public Product updateProduct(
            @PathVariable Long productId,
            @RequestBody Product request
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategoryId(request.getCategoryId() == null ? product.getCategoryId() : request.getCategoryId());
        product.setSubCategoryId(request.getSubCategoryId() == null ? product.getSubCategoryId() : request.getSubCategoryId());
        product.setUnit(request.getUnit());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockLimit(request.getLowStockLimit());
        product.setHsnSacCode(request.getHsnSacCode());
        product.setGstRate(request.getGstRate());
        product.setCgstRate(request.getCgstRate());
        product.setSgstRate(request.getSgstRate());
        product.setIgstRate(request.getIgstRate());
        product.setCessRate(request.getCessRate());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setTaxIncluded(request.getTaxIncluded());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

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

    @GetMapping("/subcategories")
public List<SubCategory> getAllSubCategories() {
    return subCategoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
}
}