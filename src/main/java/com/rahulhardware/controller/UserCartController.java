package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

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
    public List<CartItemResponse> getCart(@PathVariable String mobile) {
        return repository.findCartItemsWithProductDetails(mobile);
    }

    @PostMapping("/{mobile}/add")
    @Transactional
    public void addToCart(
            @PathVariable String mobile,
            @RequestBody Map<String, Object> body
    ) {
        String productId = String.valueOf(body.get("productId"));
        Integer quantity = Integer.parseInt(
                String.valueOf(body.getOrDefault("quantity", 1))
        );

        repository.upsertCartItem(mobile, productId, quantity);
    }

    @DeleteMapping("/{mobile}/remove/{productId}")
    @Transactional
    public void removeItem(
            @PathVariable String mobile,
            @PathVariable String productId
    ) {
        repository.deleteByUserMobileAndProductId(mobile, productId);
    }

    @DeleteMapping("/{mobile}/clear")
    @Transactional
    public void clearCart(@PathVariable String mobile) {
        repository.deleteByUserMobile(mobile);
    }
}