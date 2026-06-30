package com.rahulhardware.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "support_chat_messages")
public class SupportChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    @Column(nullable = false)
    private String senderType; // CUSTOMER / ADMIN

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String attachmentUrl;

    private String attachmentName;
    private String attachmentType;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public String getSenderType() { return senderType; }
    public String getMessage() { return message; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public String getAttachmentName() { return attachmentName; }
    public String getAttachmentType() { return attachmentType; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public void setMessage(String message) { this.message = message; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
}