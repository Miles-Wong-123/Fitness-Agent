package com.miles.fitnessagent;

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

public class RegisterActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private EditText emailInput;
    private EditText passwordInput;
    private EditText codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        codeInput = findViewById(R.id.code_input);
        Button sendCodeButton = findViewById(R.id.send_code_button);
        Button registerButton = findViewById(R.id.register_button);
        TextView loginLink = findViewById(R.id.login_link);
        ImageButton themeButton = findViewById(R.id.theme_button);

        sendCodeButton.setOnClickListener(v -> sendCode());
        registerButton.setOnClickListener(v -> register());
        loginLink.setOnClickListener(v -> finish());
        themeButton.setOnClickListener(v -> ThemeManager.toggle(this));
    }

    private void sendCode() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                JSONObject response = new JSONObject(ApiClient.post("/auth/send-code", body, null));
                String devCode = response.optString("devCode", "");
                mainHandler.post(() -> {
                    if (!devCode.isEmpty()) {
                        codeInput.setText(devCode);
                    }
                    Toast.makeText(this, "Verification code sent.", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String code = codeInput.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                body.put("code", code);
                ApiClient.post("/auth/register", body, null);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Registered. Please login.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception ex) {
                mainHandler.post(() -> Toast.makeText(this, "Registration failed: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
