package com.miles.fitnessagent.auth.dto;

public record SendCodeResponse(
        String message,
        String devCode
) {
}
