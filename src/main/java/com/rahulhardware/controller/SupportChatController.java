package com.rahulhardware.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.entity.SupportChat;
import com.rahulhardware.entity.SupportChatMessage;
import com.rahulhardware.service.SupportChatService;

@RestController
@RequestMapping("/api/support/chats")
@CrossOrigin("*")
public class SupportChatController {

    private final SupportChatService service;

    public SupportChatController(SupportChatService service) {
        this.service = service;
    }

    @PostMapping("/customer/start")
    public SupportChat startCustomerChat(@RequestBody Map<String, Object> body) {
        Long orderId = null;

        Object orderIdValue = body.get("orderId");
        if (orderIdValue != null && !String.valueOf(orderIdValue).isBlank()) {
            orderId = Long.parseLong(String.valueOf(orderIdValue));
        }

        return service.getOrCreateCustomerChat(
                orderId,
                String.valueOf(body.get("customerMobile")),
                String.valueOf(body.get("customerName"))
        );
    }

    @GetMapping("/customer/existing")
    public ResponseEntity<SupportChat> getExistingCustomerChat(
            @RequestParam Long orderId,
            @RequestParam String customerMobile
    ) {
        return service.getExistingCustomerChat(orderId, customerMobile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/customer/{mobile}")
    public List<SupportChat> getCustomerChats(@PathVariable String mobile) {
        return service.getCustomerChats(mobile);
    }

    @GetMapping("/admin")
    public List<SupportChat> getAdminChats() {
        return service.getAdminChats();
    }

    @GetMapping("/{chatId}/messages")
    public List<SupportChatMessage> getMessages(@PathVariable Long chatId) {
        return service.getMessages(chatId);
    }

    @PostMapping(value = "/{chatId}/messages", consumes = {"multipart/form-data"})
    public SupportChatMessage sendMessage(
            @PathVariable Long chatId,
            @RequestParam String senderType,
            @RequestParam(required = false) String message,
            @RequestPart(required = false) MultipartFile file
    ) {
        return service.sendMessage(chatId, senderType, message, file);
    }

    @GetMapping("/{chatId}")
    public SupportChat getChat(@PathVariable Long chatId) {
        return service.getChat(chatId);
    }

    @PatchMapping("/{chatId}/end")
    public ResponseEntity<Void> endChat(@PathVariable Long chatId) {
        service.endChat(chatId);
        return ResponseEntity.ok().build();
    }

    
}