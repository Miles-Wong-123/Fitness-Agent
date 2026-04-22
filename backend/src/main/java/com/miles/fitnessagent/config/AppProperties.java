package com.miles.fitnessagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String environment = "local";
    private Jwt jwt = new Jwt();
    private Verification verification = new Verification();
    private Qwen qwen = new Qwen();
    private Rag rag = new Rag();

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public Qwen getQwen() {
        return qwen;
    }

    public void setQwen(Qwen qwen) {
        this.qwen = qwen;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public static class Jwt {
        private String secret;
        private long expiresMinutes = 10080;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiresMinutes() {
            return expiresMinutes;
        }

        public void setExpiresMinutes(long expiresMinutes) {
            this.expiresMinutes = expiresMinutes;
        }
    }

    public static class Verification {
        private long expiresMinutes = 10;

        public long getExpiresMinutes() {
            return expiresMinutes;
        }

        public void setExpiresMinutes(long expiresMinutes) {
            this.expiresMinutes = expiresMinutes;
        }
    }

    public static class Qwen {
        private String apiKey = "";
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String chatModel = "qwen-plus";
        private String embeddingModel = "text-embedding-v4";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }
    }

    public static class Rag {
        private int embeddingDimension = 1024;
        private int topK = 4;
        private int chunkSize = 900;
        private int chunkOverlap = 120;

        public int getEmbeddingDimension() {
            return embeddingDimension;
        }

        public void setEmbeddingDimension(int embeddingDimension) {
            this.embeddingDimension = embeddingDimension;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public int getChunkOverlap() {
            return chunkOverlap;
        }

        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }
    }
}
