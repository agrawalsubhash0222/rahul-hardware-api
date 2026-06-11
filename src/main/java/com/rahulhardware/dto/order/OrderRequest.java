package com.rahulhardware.dto.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    private String userMobile;
    private Long addressId;

    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;

    private BigDecimal subtotalAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;

    private List<OrderItemRequest> items;

    public String getUserMobile() {
        return userMobile;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }
}