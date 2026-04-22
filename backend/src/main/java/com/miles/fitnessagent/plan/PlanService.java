package com.miles.fitnessagent.plan;

import com.miles.fitnessagent.plan.dto.MealAdviceRequest;
import com.miles.fitnessagent.plan.dto.PlanResponse;
import com.miles.fitnessagent.plan.dto.WorkoutPlanRequest;
import com.miles.fitnessagent.qwen.QwenClient;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
    private final QwenClient qwenClient;

    public PlanService(QwenClient qwenClient) {
        this.qwenClient = qwenClient;
    }

    public PlanResponse mealAdvice(MealAdviceRequest request) {
        String prompt = """
                You are Fitness Agent. Create practical meal advice in Markdown.
                Include:
                - Daily nutrition focus
                - Meal examples
                - Protein and hydration suggestions
                - Safety notes

                Goal: %s
                Dietary preference: %s
                Allergies or restrictions: %s
                User profile: %s
                """.formatted(
                request.goal(),
                value(request.dietaryPreference()),
                value(request.allergies()),
                value(request.profile())
        );
        return new PlanResponse(qwenClient.chat(prompt));
    }

    public PlanResponse workoutPlan(WorkoutPlanRequest request) {
        String prompt = """
                You are Fitness Agent. Create a realistic workout plan in Markdown.
                Include:
                - Weekly schedule
                - Warm-up
                - Main exercises with sets and reps
                - Progression advice
                - Recovery and safety notes

                Goal: %s
                Training level: %s
                Weekly frequency: %s
                Equipment: %s
                Limitations or injuries: %s
                """.formatted(
                request.goal(),
                value(request.level()),
                value(request.weeklyFrequency()),
                value(request.equipment()),
                value(request.limitations())
        );
        return new PlanResponse(qwenClient.chat(prompt));
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "Not specified" : value;
    }
}
