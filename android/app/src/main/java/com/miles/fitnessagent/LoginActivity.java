package com.miles.fitnessagent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private EditText emailInput;
    private EditText passwordInput;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openConversations();
            return;
        }

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_button);
        TextView registerLink = findViewById(R.id.register_link);
        ImageButton themeButton = findViewById(R.id.theme_button);

        loginButton.setOnClickListener(v -> login());
        registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        themeButton.setOnClickListener(v -> ThemeManager.toggle(this));
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                JSONObject response = new JSONObject(ApiClient.post("/auth/login", body, null));
                String token = response.getString("accessToken");
                sessionManager.saveSession(token, email);
                mainHandler.post(this::openConversations);
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Login failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void openConversations() {
        startActivity(new Intent(this, ConversationActivity.class));
        finish();
    }
}
