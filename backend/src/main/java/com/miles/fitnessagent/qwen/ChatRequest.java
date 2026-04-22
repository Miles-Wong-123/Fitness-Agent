package com.miles.fitnessagent.qwen;

import java.util.List;

public record ChatRequest(
        String model,
        List<ChatMessage> messages,
        Boolean stream
) {
}
