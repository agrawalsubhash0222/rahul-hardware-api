package com.rahulhardware.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SupportTicket;
import com.rahulhardware.enums.TicketStatus;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByCustomerMobileOrderByCreatedAtDesc(String customerMobile);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByStatusAndResolvedAtBefore(
            TicketStatus status,
            LocalDateTime resolvedAt
    );
    
}