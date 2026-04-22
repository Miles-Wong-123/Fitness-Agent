package com.miles.fitnessagent.chat.dto;

import com.miles.fitnessagent.knowledge.dto.SourceChunk;
import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceChunk> sources
) {
}
