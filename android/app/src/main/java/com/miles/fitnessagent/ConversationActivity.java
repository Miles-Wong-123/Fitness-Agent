package com.miles.fitnessagent;

import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
        Button mealPlanButton = findViewById(R.id.meal_plan_button);
        Button workoutPlanButton = findViewById(R.id.workout_plan_button);
        ImageButton themeButton = findViewById(R.id.theme_button);
        RecyclerView recyclerView = findViewById(R.id.conversation_recycler);

        emailText.setText(sessionManager.getEmail());
        adapter = new ConversationAdapter(conversations, this::openChat, this::showConversationMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        newButton.setOnClickListener(v -> createConversation());
        mealPlanButton.setOnClickListener(v -> openPlan("meal"));
        workoutPlanButton.setOnClickListener(v -> openPlan("workout"));
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

    private void openPlan(String type) {
        Intent intent = new Intent(this, PlanActivity.class);
        intent.putExtra("plan_type", type);
        startActivity(intent);
    }

    private void showConversationMenu(Conversation conversation, View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add("Rename");
        popupMenu.getMenu().add("Delete");
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Rename".equals(title)) {
                showRenameDialog(conversation);
            } else if ("Delete".equals(title)) {
                showDeleteDialog(conversation);
            }
            return true;
        });
        popupMenu.show();
    }

    private void showRenameDialog(Conversation conversation) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(conversation.title);
        input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this)
                .setTitle("Rename conversation")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> renameConversation(conversation, input.getText().toString().trim()))
                .show();
    }

    private void showDeleteDialog(Conversation conversation) {
        new AlertDialog.Builder(this)
                .setTitle("Delete conversation")
                .setMessage("Delete \"" + conversation.title + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (DialogInterface dialog, int which) -> deleteConversation(conversation))
                .show();
    }

    private void renameConversation(Conversation conversation, String title) {
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("title", title);
                ApiClient.patch("/conversations/" + conversation.id, body, sessionManager.getToken());
                mainHandler.post(this::loadConversations);
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Rename failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void deleteConversation(Conversation conversation) {
        executor.execute(() -> {
            try {
                ApiClient.delete("/conversations/" + conversation.id, sessionManager.getToken());
                mainHandler.post(this::loadConversations);
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Delete failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
