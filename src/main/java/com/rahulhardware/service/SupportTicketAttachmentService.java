package com.rahulhardware.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.dto.support.SupportTicketAttachmentResponse;
import com.rahulhardware.entity.SupportTicketAttachment;
import com.rahulhardware.repository.SupportTicketAttachmentRepository;

@Service
public class SupportTicketAttachmentService {

    private final SupportTicketAttachmentRepository repository;
    private final SupportTicketService ticketService;

    public SupportTicketAttachmentService(
            SupportTicketAttachmentRepository repository,
            SupportTicketService ticketService
    ) {
        this.repository = repository;
        this.ticketService = ticketService;
    }

    public SupportTicketAttachmentResponse uploadAttachment(
            Long ticketId,
            String uploadedBy,
            MultipartFile file
    ) throws IOException {
        ticketService.getTicketById(ticketId);

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        String originalName = file.getOriginalFilename() == null
                ? "attachment"
                : file.getOriginalFilename();

        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueName = UUID.randomUUID() + "_" + cleanName;

        Path uploadDir = Paths.get("uploads/support-tickets");
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/support-tickets/" + uniqueName;

        SupportTicketAttachment attachment = new SupportTicketAttachment();
        attachment.setTicketId(ticketId);
        attachment.setUploadedBy(uploadedBy == null || uploadedBy.isBlank() ? "CUSTOMER" : uploadedBy);
        attachment.setFileName(originalName);
        attachment.setFileType(file.getContentType());
        attachment.setFileUrl(fileUrl);

        SupportTicketAttachment saved = repository.save(attachment);
        return toResponse(saved);
    }

    public List<SupportTicketAttachmentResponse> getAttachments(Long ticketId) {
        ticketService.getTicketById(ticketId);

        return repository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SupportTicketAttachmentResponse toResponse(SupportTicketAttachment item) {
        return new SupportTicketAttachmentResponse(
                item.getId(),
                item.getTicketId(),
                item.getUploadedBy(),
                item.getFileName(),
                item.getFileType(),
                item.getFileUrl(),
                item.getCreatedAt()
        );
    }
}