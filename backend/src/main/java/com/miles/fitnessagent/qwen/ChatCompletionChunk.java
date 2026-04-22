package com.miles.fitnessagent.qwen;

import java.util.List;

public record ChatCompletionChunk(
        List<Choice> choices
) {
    public record Choice(Delta delta) {
    }

    public record Delta(String content) {
    }
}
