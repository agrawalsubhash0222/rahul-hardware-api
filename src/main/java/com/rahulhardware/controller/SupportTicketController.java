package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.support.CreateSupportTicketRequest;
import com.rahulhardware.dto.support.SupportTicketCommentResponse;
import com.rahulhardware.entity.SupportTicket;
import com.rahulhardware.service.SupportTicketService;

@RestController
@RequestMapping("/api/support/tickets")
@CrossOrigin("*")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public SupportTicket createTicket(@RequestBody CreateSupportTicketRequest request) {
        return supportTicketService.createTicket(request);
    }

    @GetMapping("/customer/{mobile}")
    public List<SupportTicket> getCustomerTickets(@PathVariable String mobile) {
        return supportTicketService.getCustomerTickets(mobile);
    }

    @GetMapping("/admin")
    public List<SupportTicket> getAllTickets() {
        return supportTicketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public SupportTicket getTicketById(@PathVariable Long id) {
        return supportTicketService.getTicketById(id);
    }

    @PatchMapping("/admin/{id}/status")
    public SupportTicket updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String status = request.get("status");
        String adminRemark = request.get("adminRemark");

        return supportTicketService.updateStatus(id, status, adminRemark);
    }

    @PatchMapping("/{id}/reopen")
    public SupportTicket reopenTicket(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String commentText = request.get("commentText");
        return supportTicketService.reopenTicket(id, commentText);
    }

    @GetMapping("/{ticketId}/comments")
    public List<SupportTicketCommentResponse> getCustomerComments(@PathVariable Long ticketId) {
        return supportTicketService.getComments(ticketId);
    }

    @PostMapping("/{ticketId}/comments")
    public SupportTicketCommentResponse addCustomerComment(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request
    ) {
        return supportTicketService.addComment(
                ticketId,
                "CUSTOMER",
                request.get("commentText")
        );
    }

    @GetMapping("/admin/{ticketId}/comments")
    public List<SupportTicketCommentResponse> getAdminComments(@PathVariable Long ticketId) {
        return supportTicketService.getComments(ticketId);
    }

    @PostMapping("/admin/{ticketId}/comments")
    public SupportTicketCommentResponse addAdminComment(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request
    ) {
        return supportTicketService.addComment(
                ticketId,
                "ADMIN",
                request.get("commentText")
        );
    }

    @PatchMapping("/{id}/close")
public SupportTicket closeTicket(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
) {
    String commentText = request.get("commentText");
    return supportTicketService.closeTicketByCustomer(id, commentText);
}
}