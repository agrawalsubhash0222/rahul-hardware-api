package com.rahulhardware.service.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rahulhardware.dto.order.OrderDetailsResponse;
import com.rahulhardware.dto.order.OrderItemRequest;
import com.rahulhardware.dto.order.OrderRequest;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.entity.OrderItem;
import com.rahulhardware.entity.UserAddress;
import com.rahulhardware.repository.CustomerOrderRepository;
import com.rahulhardware.repository.UserAddressRepository;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final UserAddressRepository addressRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            UserAddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
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

    public OrderDetailsResponse getOrderById(
            String userMobile,
            Long orderId) {

        CustomerOrder order = orderRepository
                .findByIdAndUserMobile(orderId, userMobile)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        UserAddress address = addressRepository
                .findById(order.getAddressId())
                .orElse(null);

        OrderDetailsResponse response = new OrderDetailsResponse();

        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());

        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderStatus(order.getOrderStatus());

        response.setSubtotalAmount(order.getSubtotalAmount());
        response.setDeliveryCharge(order.getDeliveryCharge());
        response.setPlatformFee(order.getPlatformFee());
        response.setTotalAmount(order.getTotalAmount());

        response.setCreatedAt(order.getCreatedAt());

        response.setItems(order.getItems());

        if (address != null) {

            response.setCustomerName(
                    (address.getFirstName() == null ? "" : address.getFirstName())
                            + " "
                            + (address.getLastName() == null ? "" : address.getLastName()));

            response.setCustomerMobile(address.getMobile());

            response.setFullAddress(address.getFullAddress());
            response.setCity(address.getCity());
            response.setState(address.getState());
            response.setPinCode(address.getPinCode());
        }

        return response;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}