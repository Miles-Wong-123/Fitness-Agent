package com.miles.fitnessagent.chat.dto;

import jakarta.validation.constraints.Size;

public record ConversationCreateRequest(
        @Size(max = 120) String title
) {
}
