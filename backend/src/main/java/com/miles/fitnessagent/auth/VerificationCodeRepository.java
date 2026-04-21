package com.miles.fitnessagent.auth;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findFirstByEmailAndCodeAndPurposeAndUsedFalseAndExpiresAtAfterOrderByIdDesc(
            String email,
            String code,
            String purpose,
            OffsetDateTime now
    );
}
