package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.SupportChatMessage;

public interface SupportChatMessageRepository extends JpaRepository<SupportChatMessage, Long> {

    List<SupportChatMessage> findByChatIdOrderByCreatedAtAsc(Long chatId);
}