package com.rahulhardware.service.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rahulhardware.entity.Product;
import com.rahulhardware.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(Product product) {
        product.setActive(true);

        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        if (product.getLowStockLimit() == null) {
            product.setLowStockLimit(10);
        }

        if (product.getImageUrl() == null) {
            product.setImageUrl("");
        }

        return productRepository.save(product);
    }

    public Product addStock(Long productId, Integer addQuantity) {
        if (addQuantity == null || addQuantity <= 0) {
            throw new RuntimeException("Stock quantity must be greater than 0");
        }

        Product product = getProductById(productId);

        int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        product.setStockQuantity(currentStock + addQuantity);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
    }

}