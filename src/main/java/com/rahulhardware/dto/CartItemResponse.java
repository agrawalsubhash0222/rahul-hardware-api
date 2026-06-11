package com.rahulhardware.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private String productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String unit;
    private Integer quantity;

    public CartItemResponse(
            String productId,
            String name,
            String imageUrl,
            BigDecimal price,
            String unit,
            Integer quantity
    ) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.unit = unit;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getQuantity() {
        return quantity;
    }
}