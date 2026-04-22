package com.miles.fitnessagent.plan.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkoutPlanRequest(
        @NotBlank String goal,
        String level,
        String weeklyFrequency,
        String equipment,
        String limitations
) {
}
