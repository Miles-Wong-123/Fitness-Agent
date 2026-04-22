package com.miles.fitnessagent.qwen;

import java.util.List;

public record ChatResponse(
        List<Choice> choices
) {
    public record Choice(ChatMessage message) {
    }
}
