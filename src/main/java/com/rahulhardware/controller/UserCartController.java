package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.CartItemResponse;
import com.rahulhardware.repository.UserCartItemRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class UserCartController {

    private final UserCartItemRepository repository;

    public UserCartController(UserCartItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{mobile}")
    public ResponseEntity<List<CartItemResponse>> getCart(@PathVariable String mobile) {
        return ResponseEntity.ok(repository.findCartItemsWithProductDetails(mobile));
    }

    @PostMapping("/{mobile}/add")
    @Transactional
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable String mobile,
            @RequestBody Map<String, Object> body) {
        Object productIdValue = body.get("productId");
        if (productIdValue == null || String.valueOf(productIdValue).isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "productId is required"));
        }

        String productId = String.valueOf(productIdValue).trim();
        Integer quantity = parseQuantity(body.getOrDefault("quantity", 1));

        repository.upsertCartItem(mobile, productId, quantity);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart updated"));
    }

    @DeleteMapping("/{mobile}/remove/{productId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeItem(
            @PathVariable String mobile,
            @PathVariable String productId) {
        repository.deleteByUserMobileAndProductId(mobile, productId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Item removed from cart"));
    }

    @DeleteMapping("/{mobile}/clear")
    @Transactional
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable String mobile) {
        repository.deleteByUserMobile(mobile);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart cleared"));
    }

    // Backward-compatible alias for older frontend calls: /api/cart/clear/{mobile}
    @DeleteMapping("/clear/{mobile}")
    @Transactional
    public ResponseEntity<Map<String, Object>> clearCartLegacy(@PathVariable String mobile) {
        return clearCart(mobile);
    }

    private Integer parseQuantity(Object quantityValue) {
        try {
            int quantity = Integer.parseInt(String.valueOf(quantityValue));
            return quantity;
        } catch (Exception ignored) {
            return 1;
        }
    }
}
