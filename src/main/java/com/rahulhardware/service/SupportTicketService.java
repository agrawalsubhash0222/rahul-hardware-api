package com.rahulhardware.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.rahulhardware.dto.support.CreateSupportTicketRequest;
import com.rahulhardware.dto.support.SupportTicketCommentResponse;
import com.rahulhardware.entity.SupportTicket;
import com.rahulhardware.entity.SupportTicketComment;
import com.rahulhardware.enums.TicketStatus;
import com.rahulhardware.repository.SupportTicketCommentRepository;
import com.rahulhardware.repository.SupportTicketRepository;

@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final SupportTicketCommentRepository commentRepository;

    public SupportTicketService(
            SupportTicketRepository ticketRepository,
            SupportTicketCommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    public SupportTicket createTicket(CreateSupportTicketRequest request) {
        SupportTicket ticket = new SupportTicket();

        ticket.setOrderId(request.getOrderId());
        ticket.setOrderNumber(request.getOrderNumber());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setCustomerMobile(request.getCustomerMobile());
        ticket.setIssueTitle(request.getTitle());
        ticket.setIssueDescription(request.getDescription());
        ticket.setContactNumber(request.getCallbackNumber());
        ticket.setStatus(TicketStatus.OPEN);

        return ticketRepository.save(ticket);
    }

    public List<SupportTicket> getCustomerTickets(String mobile) {
        return ticketRepository.findByCustomerMobileOrderByCreatedAtDesc(mobile);
    }

    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public SupportTicket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    public SupportTicket updateStatus(Long id, String status, String adminRemark) {
        SupportTicket ticket = getTicketById(id);

        if (status != null && !status.isBlank()) {
            TicketStatus newStatus = TicketStatus.valueOf(status.trim().toUpperCase());

            ticket.setStatus(newStatus);

            if (newStatus == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
                ticket.setClosedAt(null);
            }

            if (newStatus == TicketStatus.CLOSED) {
                ticket.setClosedAt(LocalDateTime.now());

                if (adminRemark == null || adminRemark.isBlank()) {
                    ticket.setAdminRemark(
                            "This ticket has been automatically closed because there was no response within 48 hours. If the issue still persists, please raise a new support ticket.");
                }
            }

            if (newStatus == TicketStatus.REVIEW || newStatus == TicketStatus.RE_OPENED) {
                ticket.setResolvedAt(null);
                ticket.setClosedAt(null);
            }
        }

        if (adminRemark != null && !adminRemark.isBlank()) {
            ticket.setAdminRemark(adminRemark.trim());
        }

        return ticketRepository.save(ticket);
    }

    public SupportTicket reopenTicket(Long id, String customerComment) {
        SupportTicket ticket = getTicketById(id);

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new RuntimeException("Only resolved tickets can be reopened");
        }

        if (ticket.getResolvedAt() == null ||
                ticket.getResolvedAt().isBefore(LocalDateTime.now().minusHours(48))) {
            throw new RuntimeException("Reopen window expired");
        }

        ticket.setStatus(TicketStatus.RE_OPENED);
        ticket.setResolvedAt(null);
        ticket.setClosedAt(null);

        SupportTicket savedTicket = ticketRepository.save(ticket);

        if (customerComment != null && !customerComment.isBlank()) {
            addComment(id, "CUSTOMER", customerComment.trim());
        }

        return savedTicket;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void autoCloseResolvedTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);

        List<SupportTicket> tickets = ticketRepository.findByStatusAndResolvedAtBefore(TicketStatus.RESOLVED, cutoff);

        for (SupportTicket ticket : tickets) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());
            ticket.setAdminRemark(
                    "This ticket has been automatically closed because there was no response within 48 hours. If the issue still persists, please raise a new support ticket.");
        }

        ticketRepository.saveAll(tickets);
    }

    public List<SupportTicketCommentResponse> getComments(Long ticketId) {
        getTicketById(ticketId);

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(comment -> new SupportTicketCommentResponse(
                        comment.getId(),
                        comment.getTicketId(),
                        comment.getCommentedBy(),
                        comment.getCommentText(),
                        comment.getCreatedAt()))
                .toList();
    }

    public SupportTicketCommentResponse addComment(Long ticketId, String commentedBy, String commentText) {
        getTicketById(ticketId);

        if (commentText == null || commentText.isBlank()) {
            throw new RuntimeException("Comment text is required");
        }

        SupportTicketComment comment = new SupportTicketComment();
        comment.setTicketId(ticketId);
        comment.setCommentedBy(commentedBy);
        comment.setCommentText(commentText.trim());

        SupportTicketComment saved = commentRepository.save(comment);

        return new SupportTicketCommentResponse(
                saved.getId(),
                saved.getTicketId(),
                saved.getCommentedBy(),
                saved.getCommentText(),
                saved.getCreatedAt());
    }

    public SupportTicket closeTicketByCustomer(Long ticketId, String commentText) {
    SupportTicket ticket = getTicketById(ticketId);

    if (ticket.getStatus() == TicketStatus.CLOSED) {
        return ticket;
    }

    ticket.setStatus(TicketStatus.CLOSED);
    ticket.setClosedAt(LocalDateTime.now());
    ticket.setResolvedAt(null);

    SupportTicket savedTicket = ticketRepository.save(ticket);

    String finalCloseComment =
            commentText != null && !commentText.trim().isEmpty()
                    ? commentText.trim()
                    : "Ticket closed by Customer.";

    addComment(ticketId, "CUSTOMER", finalCloseComment);

    return savedTicket;
}
}