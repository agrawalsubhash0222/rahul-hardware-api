package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SupportChat;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {

    Optional<SupportChat> findByOrderIdAndCustomerMobile(Long orderId, String customerMobile);

    List<SupportChat> findByCustomerMobileOrderByUpdatedAtDesc(String customerMobile);

    List<SupportChat> findAllByOrderByUpdatedAtDesc();

    
}