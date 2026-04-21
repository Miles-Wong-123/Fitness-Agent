package com.miles.fitnessagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String environment = "local";
    private Jwt jwt = new Jwt();
    private Verification verification = new Verification();

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
}
