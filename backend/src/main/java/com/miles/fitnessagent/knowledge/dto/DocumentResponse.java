package com.miles.fitnessagent.knowledge.dto;

import java.time.OffsetDateTime;

public record DocumentResponse(
        Long id,
        String title,
        OffsetDateTime createdAt,
        int chunkCount
) {
}
