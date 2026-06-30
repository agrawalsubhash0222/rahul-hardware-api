package com.rahulhardware.dto.support;

import java.time.LocalDateTime;

public class SupportTicketCommentResponse {

    private Long id;
    private Long ticketId;
    private String commentedBy;
    private String commentText;
    private LocalDateTime createdAt;

    public SupportTicketCommentResponse(
            Long id,
            Long ticketId,
            String commentedBy,
            String commentText,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.ticketId = ticketId;
        this.commentedBy = commentedBy;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public String getCommentText() {
        return commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}