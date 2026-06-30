package com.rahulhardware.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rahulhardware.dto.admin.AdminCustomerResponse;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.repository.CustomerOrderRepository;

@Service
public class AdminCustomerService {

    private final CustomerOrderRepository customerOrderRepository;

    public AdminCustomerService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    public List<AdminCustomerResponse> getCustomers() {
        return customerOrderRepository.getAdminCustomers();
    }

    public List<CustomerOrder> getCustomerOrders(String mobile) {
        return customerOrderRepository.findCustomerOrdersByMobile(mobile);
    }
}