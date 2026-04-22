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

public class PlanActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;
    private String type;
    private TextView titleText;
    private TextView resultText;
    private EditText goalInput;
    private EditText detailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        sessionManager = new SessionManager(this);
        type = getIntent().getStringExtra("plan_type");
        if (type == null) {
            type = "meal";
        }

        titleText = findViewById(R.id.plan_title);
        resultText = findViewById(R.id.plan_result);
        goalInput = findViewById(R.id.goal_input);
        detailInput = findViewById(R.id.detail_input);
        Button generateButton = findViewById(R.id.generate_button);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton themeButton = findViewById(R.id.theme_button);

        boolean meal = "meal".equals(type);
        titleText.setText(meal ? "Meal Advice" : "Workout Plan");
        goalInput.setHint(meal ? "Goal, e.g. fat loss, muscle gain" : "Goal, e.g. strength, hypertrophy");
        detailInput.setHint(meal
                ? "Diet preference, allergies, body profile..."
                : "Level, weekly frequency, equipment, limitations...");

        backButton.setOnClickListener(v -> finish());
        themeButton.setOnClickListener(v -> ThemeManager.toggle(this));
        generateButton.setOnClickListener(v -> generate());
    }

    private void generate() {
        String goal = goalInput.getText().toString().trim();
        String detail = detailInput.getText().toString().trim();
        if (goal.isEmpty()) {
            Toast.makeText(this, "Please enter your goal.", Toast.LENGTH_SHORT).show();
            return;
        }
        resultText.setText("Generating...");
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("goal", goal);
                if ("meal".equals(type)) {
                    body.put("profile", detail);
                    body.put("dietaryPreference", "");
                    body.put("allergies", "");
                } else {
                    body.put("level", detail);
                    body.put("weeklyFrequency", "");
                    body.put("equipment", "");
                    body.put("limitations", "");
                }
                String path = "meal".equals(type) ? "/plans/meal" : "/plans/workout";
                JSONObject response = new JSONObject(ApiClient.post(path, body, sessionManager.getToken()));
                String content = response.getString("content");
                mainHandler.post(() -> resultText.setText(MarkdownRenderer.render(content)));
            } catch (Exception ex) {
                mainHandler.post(() -> {
                    resultText.setText("");
                    Toast.makeText(this, "Generate failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
