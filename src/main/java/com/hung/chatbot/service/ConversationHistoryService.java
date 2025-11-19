package com.hung.chatbot.service;

import com.hung.chatbot.DTO.ConversationHistoryDTO;
import com.hung.chatbot.DTO.ConversationListDTO;
import com.hung.chatbot.DTO.MessageDTO;
import com.hung.chatbot.entity.Conversation;
import com.hung.chatbot.entity.Message;
import com.hung.chatbot.repository.ConversationRepository;
import com.hung.chatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Service
@RequiredArgsConstructor
public class ConversationHistoryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Lấy danh sách tất cả conversations của user (không bao gồm messages)
     */
    @Transactional(readOnly = true)
    public List<ConversationListDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUser_UserId(userId);

        return conversations.stream()
                .map(conv -> {
                    int messageCount = messageRepository.countByConversation_ConversationId(conv.getConversationId());
                    return new ConversationListDTO(
                            conv.getConversationId(),
                            conv.getTitle(),
                            conv.getCreatedAt(),
                            conv.getLastUpdated(),
                            messageCount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một conversation cùng với tất cả messages
     */
    @Transactional(readOnly = true)
    public ConversationHistoryDTO getConversationWithMessages(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Kiểm tra quyền truy cập
        if (!conversation.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to conversation");
        }

        List<Message> messages = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);

        List<MessageDTO> messageDTOs = messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getMessageId(),
                        msg.getSender(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ConversationHistoryDTO(
                conversation.getConversationId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getLastUpdated(),
                messageDTOs
        );
    }

    /**
     * Lấy tất cả conversations của user kèm theo messages
     */
    @Transactional(readOnly = true)
    public List<ConversationHistoryDTO> getUserConversationsWithMessages(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUser_UserId(userId);

        return conversations.stream()
                .map(conv -> {
                    List<Message> messages = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(
                            conv.getConversationId());

                    List<MessageDTO> messageDTOs = messages.stream()
                            .map(msg -> new MessageDTO(
                                    msg.getMessageId(),
                                    msg.getSender(),
                                    msg.getContent(),
                                    msg.getCreatedAt()
                            ))
                            .collect(Collectors.toList());

                    return new ConversationHistoryDTO(
                            conv.getConversationId(),
                            conv.getTitle(),
                            conv.getCreatedAt(),
                            conv.getLastUpdated(),
                            messageDTOs
                    );
                })
                .collect(Collectors.toList());
    }
}