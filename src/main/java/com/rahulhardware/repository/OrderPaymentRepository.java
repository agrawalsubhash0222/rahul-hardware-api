package com.rahulhardware.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.OrderPayment;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    Optional<OrderPayment> findByOrderId(Long orderId);
}