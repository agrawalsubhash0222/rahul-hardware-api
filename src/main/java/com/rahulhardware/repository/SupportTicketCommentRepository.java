package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SupportTicketComment;

public interface SupportTicketCommentRepository extends JpaRepository<SupportTicketComment, Long> {
    List<SupportTicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}