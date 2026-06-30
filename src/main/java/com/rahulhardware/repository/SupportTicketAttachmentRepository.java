package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SupportTicketAttachment;

public interface SupportTicketAttachmentRepository extends JpaRepository<SupportTicketAttachment, Long> {
    List<SupportTicketAttachment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}