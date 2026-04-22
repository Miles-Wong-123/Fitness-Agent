package com.miles.fitnessagent.plan;

import com.miles.fitnessagent.plan.dto.MealAdviceRequest;
import com.miles.fitnessagent.plan.dto.PlanResponse;
import com.miles.fitnessagent.plan.dto.WorkoutPlanRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/meal")
    public PlanResponse mealAdvice(@Valid @RequestBody MealAdviceRequest request) {
        return planService.mealAdvice(request);
    }

    @PostMapping("/workout")
    public PlanResponse workoutPlan(@Valid @RequestBody WorkoutPlanRequest request) {
        return planService.workoutPlan(request);
    }
}
