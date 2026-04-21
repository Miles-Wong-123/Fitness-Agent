package com.miles.fitnessagent.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotNull Long conversationId,
        @NotBlank @Size(max = 4000) String message
) {
}
