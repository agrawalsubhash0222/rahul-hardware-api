package com.rahulhardware.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminCustomerResponse {

    private String mobile;
    private String customerName;
    private String email;
    private String city;
    private String state;

    private Long totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderAt;

    public AdminCustomerResponse(
            String mobile,
            String customerName,
            String email,
            String city,
            String state,
            Long totalOrders,
            BigDecimal totalSpent,
            LocalDateTime lastOrderAt
    ) {
        this.mobile = mobile;
        this.customerName = customerName;
        this.email = email;
        this.city = city;
        this.state = state;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.lastOrderAt = lastOrderAt;
    }

    public String getMobile() { return mobile; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public Long getTotalOrders() { return totalOrders; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public LocalDateTime getLastOrderAt() { return lastOrderAt; }
}