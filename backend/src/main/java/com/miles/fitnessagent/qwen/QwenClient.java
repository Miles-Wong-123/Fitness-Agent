package com.miles.fitnessagent.qwen;

import com.miles.fitnessagent.config.AppProperties;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class QwenClient {
    private final AppProperties appProperties;
    private final WebClient webClient;

    public QwenClient(AppProperties appProperties, WebClient.Builder webClientBuilder) {
        this.appProperties = appProperties;
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
                )
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
}
