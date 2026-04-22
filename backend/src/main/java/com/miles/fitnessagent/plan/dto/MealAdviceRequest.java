package com.miles.fitnessagent.plan.dto;

import jakarta.validation.constraints.NotBlank;

public record MealAdviceRequest(
        @NotBlank String goal,
        String dietaryPreference,
        String allergies,
        String profile
) {
}
