package com.rahulhardware.service.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rahulhardware.dto.order.OrderItemRequest;
import com.rahulhardware.dto.order.OrderRequest;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.entity.OrderItem;
import com.rahulhardware.repository.CustomerOrderRepository;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;

    public OrderService(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public CustomerOrder createOrder(OrderRequest request) {
        if (request.getUserMobile() == null || request.getUserMobile().isBlank()) {
            throw new RuntimeException("User mobile is required");
        }

        if (request.getAddressId() == null) {
            throw new RuntimeException("Address is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order items are required");
        }

        CustomerOrder order = new CustomerOrder();

        order.setUserMobile(request.getUserMobile());
        order.setAddressId(request.getAddressId());

        order.setPaymentMethod(defaultValue(request.getPaymentMethod(), "COD"));
        order.setPaymentStatus(defaultValue(request.getPaymentStatus(), "PENDING"));
        order.setOrderStatus(defaultValue(request.getOrderStatus(), "PLACED"));

        order.setSubtotalAmount(defaultAmount(request.getSubtotalAmount()));
        order.setDeliveryCharge(defaultAmount(request.getDeliveryCharge()));
        order.setPlatformFee(defaultAmount(request.getPlatformFee()));
        order.setTotalAmount(defaultAmount(request.getTotalAmount()));

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();

            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setProductImageUrl(itemRequest.getProductImageUrl());
            item.setUnit(itemRequest.getUnit());
            item.setPrice(defaultAmount(itemRequest.getPrice()));
            item.setQuantity(itemRequest.getQuantity());
            item.setTotalPrice(defaultAmount(itemRequest.getTotalPrice()));

            order.addItem(item);
        }

        return orderRepository.save(order);
    }

    public List<CustomerOrder> getOrdersByUserMobile(String userMobile) {
        return orderRepository.findByUserMobileOrderByCreatedAtDesc(userMobile);
    }

    public CustomerOrder getOrderById(String userMobile, Long orderId) {
    return orderRepository
            .findByIdAndUserMobile(orderId, userMobile)
            .orElseThrow(() -> new RuntimeException("Order not found"));
}

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}