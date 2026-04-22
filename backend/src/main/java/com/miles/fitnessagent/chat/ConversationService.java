package com.miles.fitnessagent.chat;

import com.miles.fitnessagent.chat.dto.ChatRequest;
import com.miles.fitnessagent.chat.dto.ChatResponse;
import com.miles.fitnessagent.chat.dto.ConversationCreateRequest;
import com.miles.fitnessagent.chat.dto.ConversationResponse;
import com.miles.fitnessagent.chat.dto.ConversationUpdateRequest;
import com.miles.fitnessagent.chat.dto.MessageResponse;
import com.miles.fitnessagent.knowledge.KnowledgeService;
import com.miles.fitnessagent.knowledge.dto.SourceChunk;
import com.miles.fitnessagent.qwen.QwenClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeService knowledgeService;
    private final QwenClient qwenClient;
    private final TransactionTemplate transactionTemplate;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            KnowledgeService knowledgeService,
            QwenClient qwenClient,
            TransactionTemplate transactionTemplate
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.knowledgeService = knowledgeService;
        this.qwenClient = qwenClient;
        this.transactionTemplate = transactionTemplate;
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
    public ConversationResponse rename(Long userId, Long conversationId, ConversationUpdateRequest request) {
        Conversation conversation = requireConversation(userId, conversationId);
        conversation.setTitle(normalizeTitle(request.title()));
        conversation.touch();
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public void delete(Long userId, Long conversationId) {
        Conversation conversation = requireConversation(userId, conversationId);
        conversationRepository.delete(conversation);
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        Conversation conversation = requireConversation(userId, request.conversationId());

        Message userMessage = new Message();
        userMessage.setConversationId(conversation.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.message());
        messageRepository.save(userMessage);

        List<SourceChunk> sources = knowledgeService.retrieve(request.message());
        String answer = qwenClient.chat(buildPrompt(request.message(), sources));
        Message assistantMessage = new Message();
        assistantMessage.setConversationId(conversation.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        messageRepository.save(assistantMessage);

        conversation.touch();
        conversationRepository.save(conversation);
        return new ChatResponse(answer, sources);
    }

    @Transactional
    public SseEmitter chatStream(Long userId, ChatRequest request) {
        Conversation conversation = requireConversation(userId, request.conversationId());

        Message userMessage = new Message();
        userMessage.setConversationId(conversation.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.message());
        messageRepository.save(userMessage);

        List<SourceChunk> sources = knowledgeService.retrieve(request.message());
        String prompt = buildPrompt(request.message(), sources);
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            StringBuilder fullAnswer = new StringBuilder();
            try {
                safeSend(emitter, "sources", sources);
                String answer = qwenClient.chatStream(prompt, token -> {
                    fullAnswer.append(token);
                    safeSend(emitter, "token", token);
                });
                String finalAnswer = fullAnswer.isEmpty() ? answer : fullAnswer.toString();
                persistAssistantMessage(conversation.getId(), finalAnswer);
                safeSend(emitter, "done", "done");
                emitter.complete();
            } catch (RuntimeException ex) {
                safeSend(emitter, "error", ex.getMessage());
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }

    private Conversation requireConversation(Long userId, Long conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private String buildPrompt(String question, List<SourceChunk> sources) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are Fitness Agent, a fitness and health knowledge assistant.
                Answer the user in the same language as the question when possible.
                Use the retrieved reference material below.
                If the material is insufficient, say so clearly.
                Do not invent medical facts. For disease, medication, injury, chest pain, fainting, severe pain, or urgent symptoms, advise consulting a qualified medical professional.

                Retrieved material:
                """);
        if (sources.isEmpty()) {
            prompt.append("(No relevant knowledge chunks were retrieved.)\n");
        } else {
            for (int i = 0; i < sources.size(); i++) {
                SourceChunk source = sources.get(i);
                prompt.append("\n[").append(i + 1).append("] ")
                        .append(source.documentTitle()).append("\n")
                        .append(source.content()).append("\n");
            }
        }
        prompt.append("\nUser question:\n").append(question);
        return prompt.toString();
    }

    private void persistAssistantMessage(Long conversationId, String answer) {
        transactionTemplate.executeWithoutResult(status -> {
            Message assistantMessage = new Message();
            assistantMessage.setConversationId(conversationId);
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(answer);
            messageRepository.save(assistantMessage);

            conversationRepository.findById(conversationId).ifPresent(conversation -> {
                conversation.touch();
                conversationRepository.save(conversation);
            });
        });
    }

    private void safeSend(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send SSE event", ex);
        }
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
