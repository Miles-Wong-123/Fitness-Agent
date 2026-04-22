package com.miles.fitnessagent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversationActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Conversation> conversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView emailText = findViewById(R.id.email_text);
        Button newButton = findViewById(R.id.new_conversation_button);
        Button logoutButton = findViewById(R.id.logout_button);
        ImageButton themeButton = findViewById(R.id.theme_button);
        RecyclerView recyclerView = findViewById(R.id.conversation_recycler);

        emailText.setText(sessionManager.getEmail());
        adapter = new ConversationAdapter(conversations, this::openChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        newButton.setOnClickListener(v -> createConversation());
        logoutButton.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        themeButton.setOnClickListener(v -> ThemeManager.toggle(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void loadConversations() {
        executor.execute(() -> {
            try {
                JSONArray array = new JSONArray(ApiClient.get("/conversations", sessionManager.getToken()));
                List<Conversation> loaded = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    loaded.add(new Conversation(
                            item.getLong("id"),
                            item.getString("title"),
                            item.optString("updatedAt", "")
                    ));
                }
                mainHandler.post(() -> {
                    conversations.clear();
                    conversations.addAll(loaded);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Load failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void createConversation() {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("title", "Fitness chat");
                JSONObject response = new JSONObject(ApiClient.post("/conversations", body, sessionManager.getToken()));
                Conversation conversation = new Conversation(
                        response.getLong("id"),
                        response.getString("title"),
                        response.optString("updatedAt", "")
                );
                mainHandler.post(() -> openChat(conversation));
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Create failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void openChat(Conversation conversation) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversation_id", conversation.id);
        intent.putExtra("conversation_title", conversation.title);
        startActivity(intent);
    }
}
