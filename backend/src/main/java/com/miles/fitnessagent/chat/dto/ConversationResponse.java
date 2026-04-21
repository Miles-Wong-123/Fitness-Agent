package com.miles.fitnessagent.chat.dto;

import java.time.OffsetDateTime;

public record ConversationResponse(
        Long id,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
