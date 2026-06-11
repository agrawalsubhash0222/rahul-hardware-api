package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserMobileOrderByCreatedAtDesc(String userMobile);

    Optional<CustomerOrder> findByIdAndUserMobile(Long id, String userMobile);
}