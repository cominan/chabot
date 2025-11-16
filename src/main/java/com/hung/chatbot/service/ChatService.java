package com.hung.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hung.chatbot.DTO.ChatRequest;
import com.hung.chatbot.DTO.ChatResponse;
import com.hung.chatbot.DTO.NLPCloudRequest;
import com.hung.chatbot.DTO.NLPCloudResponse;
import com.hung.chatbot.entity.Conversation;
import com.hung.chatbot.entity.Message;
import com.hung.chatbot.repository.ConversationRepository;
import com.hung.chatbot.repository.MessageRepository;
import com.hung.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final WebClient nlpCloudClient;

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

        StringBuilder context = new StringBuilder();
        history.reversed().forEach(m -> context.append(m.getSender()).append(": ").append(m.getContent()).append("\n"));

        // 4. Gọi AI (ở đây demo: bot trả lời lại message)
        List<NLPCloudRequest.HistoryItem> hist = history.stream().filter(m -> m.getSender().equals("user")) // lấy các cặp input/response
                .map(m -> {
                    Message botReplyMsg = history.stream().filter(x -> x.getCreatedAt().isAfter(m.getCreatedAt()) && x.getSender()
                            .equals("bot")).findFirst().orElse(null);

                    return new NLPCloudRequest.HistoryItem(m.getContent(), botReplyMsg != null ? botReplyMsg.getContent() : "");
                }).toList();

        NLPCloudRequest body = new NLPCloudRequest();
        body.setInput(req.getMessage());
        body.setContext("This is a discussion between a human and an AI assistant.");
        body.setHistory(hist);

        String rawResponse = nlpCloudClient.post()
                .uri("/chatbot")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)   // luôn nhận dạng text/plain OK
                .block();

        // Parse JSON nếu NLP Cloud trả về JSON
        ObjectMapper mapper = new ObjectMapper();
        NLPCloudResponse apiRes = mapper.readValue(rawResponse, NLPCloudResponse.class);

        String botReply = apiRes.getResponse();

        // 5. Lưu message bot vào DB
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
}
