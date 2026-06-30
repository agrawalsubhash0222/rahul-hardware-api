package com.rahulhardware.service;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.entity.SupportChat;
import com.rahulhardware.entity.SupportChatMessage;
import com.rahulhardware.enums.SupportChatStatus;
import com.rahulhardware.repository.SupportChatMessageRepository;
import com.rahulhardware.repository.SupportChatRepository;

@Service
public class SupportChatService {

    private final SupportChatRepository chatRepository;
    private final SupportChatMessageRepository messageRepository;

    public SupportChatService(
            SupportChatRepository chatRepository,
            SupportChatMessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    public SupportChat getOrCreateCustomerChat(
            Long orderId,
            String customerMobile,
            String customerName) {

        if (orderId != null) {
            return chatRepository.findByOrderIdAndCustomerMobile(orderId, customerMobile)
                    .orElseGet(() -> createChat(orderId, customerMobile, customerName));
        }

        return createChat(null, customerMobile, customerName);
    }

    public List<SupportChat> getAdminChats() {
        List<SupportChat> chats = chatRepository.findAllByOrderByUpdatedAtDesc();

        return chats.stream()
                .filter(chat -> !messageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId()).isEmpty())
                .toList();
    }

    public List<SupportChat> getCustomerChats(String mobile) {
        return chatRepository.findByCustomerMobileOrderByUpdatedAtDesc(mobile);
    }

    public SupportChat getChat(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    public List<SupportChatMessage> getMessages(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    public SupportChatMessage sendMessage(
            Long chatId,
            String senderType,
            String message,
            MultipartFile file) {

        SupportChat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getStatus() == SupportChatStatus.ENDED) {
            throw new RuntimeException("Chat has already ended");
        }

        boolean hasText = message != null && !message.trim().isEmpty();
        boolean hasFile = file != null && !file.isEmpty();

        if (!hasText && !hasFile) {
            throw new RuntimeException("Message or attachment is required");
        }

        SupportChatMessage msg = new SupportChatMessage();
        msg.setChatId(chatId);
        msg.setSenderType(senderType);
        msg.setMessage(hasText ? message.trim() : null);

        if (hasFile) {
            String url = saveFile(file);
            msg.setAttachmentUrl(url);
            msg.setAttachmentName(file.getOriginalFilename());
            msg.setAttachmentType(file.getContentType());
        }

        chatRepository.save(chat);

        return messageRepository.save(msg);
    }

    public Optional<SupportChat> getExistingCustomerChat(Long orderId, String customerMobile) {
        return chatRepository.findByOrderIdAndCustomerMobile(orderId, customerMobile);
    }

    public void endChat(Long chatId) {
        SupportChat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        chat.setStatus(SupportChatStatus.ENDED);
        chatRepository.save(chat);
    }

    private SupportChat createChat(Long orderId, String customerMobile, String customerName) {
        SupportChat chat = new SupportChat();
        chat.setOrderId(orderId);
        chat.setCustomerMobile(customerMobile);
        chat.setCustomerName(customerName);
        chat.setStatus(SupportChatStatus.ACTIVE);

        return chatRepository.save(chat);
    }

    private String saveFile(MultipartFile file) {
        try {
            String folderPath = System.getProperty("user.dir") + "/uploads/chat";
            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String originalName = file.getOriginalFilename() == null
                    ? "file"
                    : file.getOriginalFilename();

            String fileName = System.currentTimeMillis() + "_"
                    + originalName.replaceAll("\\s+", "_");

            File dest = new File(folder, fileName);
            file.transferTo(dest);

            return "/uploads/chat/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }
}