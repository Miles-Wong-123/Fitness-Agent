package com.miles.fitnessagent.chat;

import com.miles.fitnessagent.chat.dto.ChatRequest;
import com.miles.fitnessagent.chat.dto.ChatResponse;
import com.miles.fitnessagent.chat.dto.ConversationCreateRequest;
import com.miles.fitnessagent.chat.dto.ConversationResponse;
import com.miles.fitnessagent.chat.dto.MessageResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<ConversationResponse> list(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional
    public ConversationResponse create(Long userId, ConversationCreateRequest request) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(normalizeTitle(request.title()));
        return toConversationResponse(conversationRepository.save(conversation));
    }

    public List<MessageResponse> messages(Long userId, Long conversationId) {
        requireConversation(userId, conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        Conversation conversation = requireConversation(userId, request.conversationId());

        Message userMessage = new Message();
        userMessage.setConversationId(conversation.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.message());
        messageRepository.save(userMessage);

        String answer = placeholderAnswer(request.message());
        Message assistantMessage = new Message();
        assistantMessage.setConversationId(conversation.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        messageRepository.save(assistantMessage);

        conversation.touch();
        conversationRepository.save(conversation);
        return new ChatResponse(answer);
    }

    private Conversation requireConversation(Long userId, Long conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private String placeholderAnswer(String question) {
        return "Fitness Agent has received your question: \"" + question + "\". "
                + "The Spring Boot backend, login, JWT, PostgreSQL, and chat history are working. "
                + "Next milestone is connecting pgvector retrieval and Qwen generation.";
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "New chat";
        }
        return title.trim();
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
