package com.miles.fitnessagent.chat;

import com.miles.fitnessagent.chat.dto.ChatRequest;
import com.miles.fitnessagent.chat.dto.ChatResponse;
import com.miles.fitnessagent.common.CurrentUser;
import com.miles.fitnessagent.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ConversationService conversationService;
    private final CurrentUser currentUser;

    public ChatController(ConversationService conversationService, CurrentUser currentUser) {
        this.conversationService = conversationService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        AuthUser user = currentUser.require(authentication);
        return conversationService.chat(user.id(), request);
    }
}
