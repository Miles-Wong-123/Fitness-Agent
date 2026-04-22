package com.miles.fitnessagent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private SessionManager sessionManager;
    private long conversationId;
    private EditText messageInput;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        conversationId = getIntent().getLongExtra("conversation_id", -1);
        String title = getIntent().getStringExtra("conversation_title");

        TextView titleText = findViewById(R.id.chat_title);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton themeButton = findViewById(R.id.theme_button);
        ImageButton sendButton = findViewById(R.id.send_button);
        messageInput = findViewById(R.id.message_input);
        recyclerView = findViewById(R.id.message_recycler);

        titleText.setText(title == null ? "Fitness chat" : title);
        adapter = new MessageAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
        themeButton.setOnClickListener(v -> ThemeManager.toggle(this));
        sendButton.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        executor.execute(() -> {
            try {
                JSONArray array = new JSONArray(ApiClient.get("/conversations/" + conversationId + "/messages", sessionManager.getToken()));
                List<Message> loaded = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    loaded.add(new Message(item.getString("role"), item.getString("content")));
                }
                mainHandler.post(() -> {
                    messages.clear();
                    messages.addAll(loaded);
                    adapter.notifyDataSetChanged();
                    scrollToBottom();
                });
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Load failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        messageInput.setText("");
        Message userMessage = new Message("user", text);
        Message assistantMessage = new Message("assistant", "");
        messages.add(userMessage);
        messages.add(assistantMessage);
        adapter.notifyItemRangeInserted(messages.size() - 2, 2);
        scrollToBottom();

        executor.execute(() -> streamChat(text, assistantMessage));
    }

    private void streamChat(String text, Message assistantMessage) {
        try {
            JSONObject body = new JSONObject();
            body.put("conversationId", conversationId);
            body.put("message", text);
            HttpURLConnection conn = ApiClient.openStream("/chat/stream", body, sessionManager.getToken());
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP " + code);
            }
            String currentEvent = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event:")) {
                        currentEvent = line.substring(6).trim();
                    } else if (line.startsWith("data:")) {
                        String data = line.substring(5);
                        if ("token".equals(currentEvent)) {
                            appendToken(assistantMessage, data);
                        } else if ("error".equals(currentEvent)) {
                            appendToken(assistantMessage, "\n" + data);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            mainHandler.post(() -> Toast.makeText(this, "Chat failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void appendToken(Message message, String token) {
        mainHandler.post(() -> {
            message.content = message.content + token;
            adapter.notifyItemChanged(messages.indexOf(message));
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerView.scrollToPosition(messages.size() - 1);
        }
    }
}
