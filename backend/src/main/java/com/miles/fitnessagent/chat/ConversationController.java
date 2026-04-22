package com.miles.fitnessagent.chat;

import com.miles.fitnessagent.chat.dto.ConversationCreateRequest;
import com.miles.fitnessagent.chat.dto.ConversationResponse;
import com.miles.fitnessagent.chat.dto.ConversationUpdateRequest;
import com.miles.fitnessagent.chat.dto.MessageResponse;
import com.miles.fitnessagent.common.CurrentUser;
import com.miles.fitnessagent.security.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final CurrentUser currentUser;

    public ConversationController(ConversationService conversationService, CurrentUser currentUser) {
        this.conversationService = conversationService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ConversationResponse> list(Authentication authentication) {
        AuthUser user = currentUser.require(authentication);
        return conversationService.list(user.id());
    }

    @PostMapping
    public ConversationResponse create(
            @Valid @RequestBody ConversationCreateRequest request,
            Authentication authentication
    ) {
        AuthUser user = currentUser.require(authentication);
        return conversationService.create(user.id(), request);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> messages(
            @PathVariable Long conversationId,
            Authentication authentication
    ) {
        AuthUser user = currentUser.require(authentication);
        return conversationService.messages(user.id(), conversationId);
    }

    @PatchMapping("/{conversationId}")
    public ConversationResponse rename(
            @PathVariable Long conversationId,
            @Valid @RequestBody ConversationUpdateRequest request,
            Authentication authentication
    ) {
        AuthUser user = currentUser.require(authentication);
        return conversationService.rename(user.id(), conversationId, request);
    }

    @DeleteMapping("/{conversationId}")
    public void delete(
            @PathVariable Long conversationId,
            Authentication authentication
    ) {
        AuthUser user = currentUser.require(authentication);
        conversationService.delete(user.id(), conversationId);
    }
}
