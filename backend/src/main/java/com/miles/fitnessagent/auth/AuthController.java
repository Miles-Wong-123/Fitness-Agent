package com.miles.fitnessagent.auth;

import com.miles.fitnessagent.auth.dto.AuthResponse;
import com.miles.fitnessagent.auth.dto.LoginRequest;
import com.miles.fitnessagent.auth.dto.RegisterRequest;
import com.miles.fitnessagent.auth.dto.SendCodeRequest;
import com.miles.fitnessagent.auth.dto.SendCodeResponse;
import com.miles.fitnessagent.auth.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public SendCodeResponse sendCode(@Valid @RequestBody SendCodeRequest request) {
        return authService.sendRegisterCode(request.email());
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
