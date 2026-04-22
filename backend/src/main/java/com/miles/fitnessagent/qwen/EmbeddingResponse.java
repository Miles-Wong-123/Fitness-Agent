package com.miles.fitnessagent.qwen;

import java.util.List;

public record EmbeddingResponse(
        List<EmbeddingData> data
) {
    public record EmbeddingData(List<Double> embedding) {
    }
}
