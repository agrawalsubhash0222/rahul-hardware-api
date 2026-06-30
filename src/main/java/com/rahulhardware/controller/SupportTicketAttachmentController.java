package com.rahulhardware.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.dto.support.SupportTicketAttachmentResponse;
import com.rahulhardware.service.SupportTicketAttachmentService;

@RestController
@RequestMapping("/api/support/tickets")
@CrossOrigin("*")
public class SupportTicketAttachmentController {

    private final SupportTicketAttachmentService service;

    public SupportTicketAttachmentController(SupportTicketAttachmentService service) {
        this.service = service;
    }

    @GetMapping("/{ticketId}/attachments")
    public List<SupportTicketAttachmentResponse> getAttachments(@PathVariable Long ticketId) {
        return service.getAttachments(ticketId);
    }

    @PostMapping("/{ticketId}/attachments")
    public SupportTicketAttachmentResponse uploadCustomerAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return service.uploadAttachment(ticketId, "CUSTOMER", file);
    }

    @GetMapping("/admin/{ticketId}/attachments")
    public List<SupportTicketAttachmentResponse> getAdminAttachments(@PathVariable Long ticketId) {
        return service.getAttachments(ticketId);
    }

    @PostMapping("/admin/{ticketId}/attachments")
    public SupportTicketAttachmentResponse uploadAdminAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return service.uploadAttachment(ticketId, "ADMIN", file);
    }
}