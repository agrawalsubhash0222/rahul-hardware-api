package com.rahulhardware.dto.support;

import java.time.LocalDateTime;

public class SupportTicketAttachmentResponse {

    private Long id;
    private Long ticketId;
    private String uploadedBy;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private LocalDateTime createdAt;

    public SupportTicketAttachmentResponse(
            Long id,
            Long ticketId,
            String uploadedBy,
            String fileName,
            String fileType,
            String fileUrl,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.ticketId = ticketId;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTicketId() { return ticketId; }
    public String getUploadedBy() { return uploadedBy; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public String getFileUrl() { return fileUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}