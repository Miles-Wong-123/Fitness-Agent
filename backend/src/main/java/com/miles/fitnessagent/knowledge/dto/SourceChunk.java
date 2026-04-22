package com.miles.fitnessagent.knowledge.dto;

public record SourceChunk(
        Long documentId,
        String documentTitle,
        String content,
        double distance
) {
}
