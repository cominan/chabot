package com.hung.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hung.chatbot.DTO.ChatRequest;
import com.hung.chatbot.DTO.ChatResponse;
import com.hung.chatbot.DTO.CreateConversationRequest;
import com.hung.chatbot.DTO.CreateConversationResponse;
import com.hung.chatbot.DTO.NLPCloudRequest;
import com.hung.chatbot.DTO.NLPCloudResponse;
import com.hung.chatbot.entity.Conversation;
import com.hung.chatbot.entity.Message;
import com.hung.chatbot.entity.User;
import com.hung.chatbot.repository.ConversationRepository;
import com.hung.chatbot.repository.MessageRepository;
import com.hung.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final RestClient nlpCloudClient;

    public ChatResponse handleChat(ChatRequest req) throws JsonProcessingException {

        // 1. Tạo conversation nếu chưa có
        Conversation conv = conversationRepo.findById(
                req.getConversationId()).orElseGet(() -> {
            Conversation c = new Conversation();
            c.setUser(userRepo.findById(req.getUserId()).orElseThrow());
            c.setTitle("New Conversation");
            return conversationRepo.save(c);
        });

        // 2. Lưu message từ user
        Message userMsg = new Message();
        userMsg.setConversation(conv);
        userMsg.setSender("user");
        userMsg.setContent(req.getMessage());
        messageRepo.save(userMsg);

        // 3. Lấy lịch sử 20 tin gần nhất
        List<Message> history = messageRepo.findTop20ByConversation_ConversationIdOrderByCreatedAtDesc(conv.getConversationId());

        // 4. Xây dựng history theo định dạng NLP Cloud API
        // History phải là các cặp input-response theo thứ tự thời gian
        List<NLPCloudRequest.HistoryItem> hist = new java.util.ArrayList<>();
        List<Message> sortedHistory = new java.util.ArrayList<>(history);
        sortedHistory.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())); // Sắp xếp theo thời gian tăng dần

        for (int i = 0; i < sortedHistory.size() - 1; i++) {
            Message current = sortedHistory.get(i);
            Message next = sortedHistory.get(i + 1);

            // Tìm cặp user -> bot
            if (current.getSender().equals("user") && next.getSender().equals("bot")) {
                hist.add(new NLPCloudRequest.HistoryItem(current.getContent(), next.getContent()));
            }
        }

        // 5. Tạo request body cho NLP Cloud API
        NLPCloudRequest body = new NLPCloudRequest();
        body.setInput(req.getMessage());
        body.setContext("This is a discussion between a human and an AI assistant.");
        body.setHistory(hist);

        // 6. Gọi NLP Cloud API
        String rawResponse = nlpCloudClient.post()
                .uri("/chatbot")
                .body(body)
                .retrieve().toString();

        // 7. Parse JSON response từ NLP Cloud
        ObjectMapper mapper = new ObjectMapper();
        NLPCloudResponse apiRes = mapper.readValue(rawResponse, NLPCloudResponse.class);

        String botReply = apiRes.getResponse();

        // 8. Lưu message bot vào DB
        Message botMsg = new Message();
        botMsg.setConversation(conv);
        botMsg.setSender("bot");
        botMsg.setContent(botReply);
        messageRepo.save(botMsg);

        // 6. Trả response
        ChatResponse res = new ChatResponse();
        res.setConversationId(conv.getConversationId());
        res.setBotReply(botReply);

        return res;
    }

    /**
     * Tạo conversation mới
     */
    public CreateConversationResponse createConversation(CreateConversationRequest request, Long userId) {
        // 1. Kiểm tra user có tồn tại không
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. Tạo conversation mới
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(request.getTitle() != null && !request.getTitle().isEmpty()
                ? request.getTitle()
                : "New Conversation");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastUpdated(LocalDateTime.now());

        // 3. Lưu vào database
        Conversation savedConversation = conversationRepo.save(conversation);

        // 4. Tạo response
        CreateConversationResponse response = new CreateConversationResponse();
        response.setConversationId(savedConversation.getConversationId());
        response.setUserId(savedConversation.getUser().getUserId());
        response.setTitle(savedConversation.getTitle());
        response.setCreatedAt(savedConversation.getCreatedAt());
        response.setMessage("Conversation created successfully");

        return response;
    }
}
