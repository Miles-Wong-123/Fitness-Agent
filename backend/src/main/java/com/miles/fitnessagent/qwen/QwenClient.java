package com.miles.fitnessagent.qwen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miles.fitnessagent.config.AppProperties;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class QwenClient {
    private final AppProperties appProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public QwenClient(AppProperties appProperties, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(appProperties.getQwen().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isConfigured() {
        return appProperties.getQwen().getApiKey() != null && !appProperties.getQwen().getApiKey().isBlank();
    }

    public List<Double> embed(String text) {
        if (!isConfigured()) {
            return LocalEmbedding.embed(text, appProperties.getRag().getEmbeddingDimension());
        }
        EmbeddingResponse response = webClient.post()
                .uri("/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getQwen().getApiKey())
                .bodyValue(new EmbeddingRequest(appProperties.getQwen().getEmbeddingModel(), text))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();
        if (response == null || response.data() == null || response.data().isEmpty()) {
            return LocalEmbedding.embed(text, appProperties.getRag().getEmbeddingDimension());
        }
        return response.data().getFirst().embedding();
    }

    public String chat(String prompt) {
        if (!isConfigured()) {
            return "Qwen API key is not configured yet. Retrieved knowledge is ready, but generation is using fallback mode.\n\n"
                    + prompt;
        }
        ChatRequest request = new ChatRequest(
                appProperties.getQwen().getChatModel(),
                List.of(
                        new ChatMessage("system", "You are Fitness Agent, a careful fitness and health knowledge assistant."),
                        new ChatMessage("user", prompt)
                ),
                false
        );
        ChatResponse response = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getQwen().getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .block();
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "Qwen returned an empty response.";
        }
        return response.choices().getFirst().message().content();
    }

    public String chatStream(String prompt, Consumer<String> tokenConsumer) {
        if (!isConfigured()) {
            String fallback = "Qwen API key is not configured yet. Retrieved knowledge is ready, but generation is using fallback mode.\n\n"
                    + prompt;
            tokenConsumer.accept(fallback);
            return fallback;
        }
        ChatRequest request = new ChatRequest(
                appProperties.getQwen().getChatModel(),
                List.of(
                        new ChatMessage("system", "You are Fitness Agent, a careful fitness and health knowledge assistant."),
                        new ChatMessage("user", prompt)
                ),
                true
        );
        StringBuilder fullAnswer = new StringBuilder();
        try {
            Flux<String> stream = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + appProperties.getQwen().getApiKey())
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class);

            stream.toIterable().forEach(rawEvent -> {
                for (String line : rawEvent.split("\\R")) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("data:")) {
                        trimmed = trimmed.substring(5).trim();
                    }
                    if (trimmed.isBlank() || "[DONE]".equals(trimmed)) {
                        continue;
                    }
                    String content = extractContent(trimmed);
                    if (content != null && !content.isEmpty()) {
                        fullAnswer.append(content);
                        tokenConsumer.accept(content);
                    }
                }
            });
        } catch (WebClientResponseException ex) {
            String message = "Qwen stream request failed: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString();
            tokenConsumer.accept(message);
            return message;
        }
        return fullAnswer.toString();
    }

    private String extractContent(String eventJson) {
        try {
            ChatCompletionChunk chunk = objectMapper.readValue(eventJson, ChatCompletionChunk.class);
            if (chunk.choices() == null || chunk.choices().isEmpty()) {
                return null;
            }
            ChatCompletionChunk.Delta delta = chunk.choices().getFirst().delta();
            if (delta == null) {
                return null;
            }
            return delta.content();
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
