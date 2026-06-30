package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.rahulhardware.dto.CartItemResponse;
import com.rahulhardware.entity.UserCartItem;

import jakarta.transaction.Transactional;

public interface UserCartItemRepository extends JpaRepository<UserCartItem, Long> {

    @Query("""
        SELECT new com.rahulhardware.dto.CartItemResponse(
            CAST(p.id AS string),
            p.name,
            p.imageUrl,
            p.price,
            p.unit,
            c.quantity
        )
        FROM UserCartItem c
        JOIN Product p ON CAST(p.id AS string) = c.productId
        WHERE c.userMobile = :userMobile
        ORDER BY c.updatedAt DESC
    """)
    List<CartItemResponse> findCartItemsWithProductDetails(String userMobile);
    List<UserCartItem> findByUserMobile(String userMobile);

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO user_cart_items
            (user_mobile, product_id, quantity, created_at, updated_at)
            VALUES (:userMobile, :productId, :quantity, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
            quantity = GREATEST(quantity + VALUES(quantity), 0),
            updated_at = NOW()
        """,
        nativeQuery = true
    )
    void upsertCartItem(String userMobile, String productId, Integer quantity);

    @Modifying
    @Transactional
    void deleteByUserMobileAndProductId(String userMobile, String productId);

    @Modifying
    @Transactional
    void deleteByUserMobile(String userMobile);
}