package com.hung.chatbot.repository;

import com.hung.chatbot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop20ByConversation_ConversationIdOrderByCreatedAtDesc(Long conversationId);
}
