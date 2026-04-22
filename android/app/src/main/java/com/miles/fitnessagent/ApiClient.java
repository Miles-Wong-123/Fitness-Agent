package com.miles.fitnessagent;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    public static final String BASE_URL = "http://43.129.185.36:8080/api";

    public static String get(String path, String token) throws Exception {
        HttpURLConnection conn = open(path, "GET", token);
        return readResponse(conn);
    }

    public static String post(String path, JSONObject body, String token) throws Exception {
        return sendJson(path, "POST", body, token);
    }

    public static String patch(String path, JSONObject body, String token) throws Exception {
        return sendJson(path, "PATCH", body, token);
    }

    public static String delete(String path, String token) throws Exception {
        HttpURLConnection conn = open(path, "DELETE", token);
        return readResponse(conn);
    }

    private static String sendJson(String path, String method, JSONObject body, String token) throws Exception {
        HttpURLConnection conn = open(path, method, token);
        conn.setDoOutput(true);
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(bytes);
        }
        return readResponse(conn);
    }

    public static HttpURLConnection openStream(String path, JSONObject body, String token) throws Exception {
        HttpURLConnection conn = open(path, "POST", token);
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setDoOutput(true);
        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        return conn;
    }

    private static HttpURLConnection open(String path, String method, String token) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(0);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        if (code < 200 || code >= 300) {
            throw new RuntimeException(builder.toString());
        }
        return builder.toString();
    }
}
