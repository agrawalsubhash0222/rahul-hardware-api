package com.rahulhardware.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.admin.AdminCustomerResponse;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.service.admin.AdminCustomerService;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin("*")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public List<AdminCustomerResponse> getCustomers() {
        return adminCustomerService.getCustomers();
    }

    @GetMapping("/{mobile}/orders")
    public List<CustomerOrder> getCustomerOrders(@PathVariable String mobile) {
        return adminCustomerService.getCustomerOrders(mobile);
    }
}