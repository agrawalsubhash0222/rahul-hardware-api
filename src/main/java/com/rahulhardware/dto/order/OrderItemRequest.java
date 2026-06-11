package com.rahulhardware.dto.order;

import java.math.BigDecimal;

public class OrderItemRequest {

    private Long productId;
    private String productName;
    private String productImageUrl;
    private String unit;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}