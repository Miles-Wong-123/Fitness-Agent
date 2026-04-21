package com.miles.fitnessagent.auth;

import com.miles.fitnessagent.auth.dto.AuthResponse;
import com.miles.fitnessagent.auth.dto.LoginRequest;
import com.miles.fitnessagent.auth.dto.RegisterRequest;
import com.miles.fitnessagent.auth.dto.SendCodeResponse;
import com.miles.fitnessagent.auth.dto.UserResponse;
import com.miles.fitnessagent.config.AppProperties;
import com.miles.fitnessagent.security.JwtService;
import com.miles.fitnessagent.user.User;
import com.miles.fitnessagent.user.UserRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            VerificationCodeRepository verificationCodeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @Transactional
    public SendCodeResponse sendRegisterCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setPurpose("register");
        verificationCode.setExpiresAt(OffsetDateTime.now().plusMinutes(appProperties.getVerification().getExpiresMinutes()));
        verificationCodeRepository.save(verificationCode);

        String devCode = "local".equalsIgnoreCase(appProperties.getEnvironment()) ? code : null;
        return new SendCodeResponse("Verification code generated", devCode);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }
        VerificationCode code = verificationCodeRepository
                .findFirstByEmailAndCodeAndPurposeAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
                        request.email(),
                        request.code(),
                        "register",
                        OffsetDateTime.now()
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification code"));

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        code.setUsed(true);
        return new UserResponse(saved.getId(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        String token = jwtService.createToken(user.getId());
        return new AuthResponse(token, "Bearer", new UserResponse(user.getId(), user.getEmail()));
    }
}
