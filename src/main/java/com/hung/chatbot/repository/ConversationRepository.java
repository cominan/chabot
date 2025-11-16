package com.hung.chatbot.repository;

import com.hung.chatbot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser_UserId(Long userId);
}
