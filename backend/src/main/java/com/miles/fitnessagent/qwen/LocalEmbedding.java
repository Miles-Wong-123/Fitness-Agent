package com.miles.fitnessagent.qwen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public final class LocalEmbedding {
    private LocalEmbedding() {
    }

    public static List<Double> embed(String text, int dimension) {
        double[] values = new double[dimension];
        for (String token : text.toLowerCase().split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            byte[] digest = sha256(token);
            int index = Math.floorMod(bytesToInt(digest), dimension);
            double sign = (digest[4] & 1) == 0 ? 1.0 : -1.0;
            values[index] += sign;
        }
        double norm = 0.0;
        for (double value : values) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            norm = 1.0;
        }
        List<Double> result = new ArrayList<>(dimension);
        for (double value : values) {
            result.add(value / norm);
        }
        return result;
    }

    private static byte[] sha256(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xff) << 24)
                | ((bytes[1] & 0xff) << 16)
                | ((bytes[2] & 0xff) << 8)
                | (bytes[3] & 0xff);
    }
}
