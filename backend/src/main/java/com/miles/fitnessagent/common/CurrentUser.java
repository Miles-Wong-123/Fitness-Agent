package com.miles.fitnessagent.common;

import com.miles.fitnessagent.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public AuthUser require(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new IllegalStateException("Missing authenticated user");
        }
        return authUser;
    }
}
