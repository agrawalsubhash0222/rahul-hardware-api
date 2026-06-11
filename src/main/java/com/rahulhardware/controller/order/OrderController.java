package com.rahulhardware.controller.order;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.order.OrderRequest;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.service.order.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public CustomerOrder createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{mobile}")
    public List<CustomerOrder> getOrdersByMobile(@PathVariable String mobile) {
        return orderService.getOrdersByUserMobile(mobile);
    }

    @GetMapping("/{mobile}/{orderId}")
    public CustomerOrder getOrderById(
            @PathVariable String mobile,
            @PathVariable Long orderId
    ) {
        return orderService.getOrderById(mobile, orderId);
    }
}