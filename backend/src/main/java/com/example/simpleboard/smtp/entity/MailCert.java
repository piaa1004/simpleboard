package com.example.simpleboard.smtp.entity;

import com.example.simpleboard.common.config.BooleanConverter;
import com.example.simpleboard.common.response.message.MailMessage;
import com.example.simpleboard.smtp.exception.MailException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 이메일 한 건에 대한 인증 상태. 이메일당 한 행만 유지한다.
 *
 * 인증에 성공해도 행을 지우지 않고 verified 로 표시만 한다.
 * 회원가입이 이 표시를 확인해야 하기 때문이다. 지워버리면 /registers 를 직접 호출해
 * 인증을 건너뛸 수 있다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailCert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailCertId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String verificationCode;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** 틀린 횟수. 무제한으로 시도하면 6자리 숫자는 금방 뚫린다. */
    @Column(nullable = false)
    private int failedAttempts;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private boolean verified;

    private LocalDateTime verifiedAt;

    private MailCert(String email) {
        this.email = email;
    }

    public static MailCert createMailCert(String email, String code, LocalDateTime now, Duration ttl) {
        MailCert mailCert = new MailCert(email);
        mailCert.issue(code, now, ttl);

        return mailCert;
    }

    /** 재발급. 이전 코드와 인증 상태·실패 횟수를 모두 초기화한다. */
    public void issue(String code, LocalDateTime now, Duration ttl) {
        this.verificationCode = code;
        this.issuedAt = now;
        this.expiresAt = now.plus(ttl);
        this.failedAttempts = 0;
        this.verified = false;
        this.verifiedAt = null;
    }

    public boolean isResendTooSoon(LocalDateTime now, Duration cooldown) {
        return now.isBefore(issuedAt.plus(cooldown));
    }

    /** 코드 검증. 실패하면 이유를 담아 예외를 던지고, 성공하면 verified 로 표시한다. */
    public void verify(String code, LocalDateTime now, int maxAttempts) {
        if (now.isAfter(expiresAt)) {
            throw new MailException(MailMessage.EXPIRED_CODE);
        }

        if (failedAttempts >= maxAttempts) {
            throw new MailException(MailMessage.TOO_MANY_ATTEMPTS);
        }

        if (!verificationCode.equals(code)) {
            failedAttempts += 1;
            throw new MailException(MailMessage.NOT_MATCHED_CODE);
        }

        this.verified = true;
        this.verifiedAt = now;
    }

    /**
     * 인증을 마친 뒤 회원가입까지 이어지는 데 허용하는 시간.
     * 인증만 해두고 한참 뒤에 가입하는 것을 막는다.
     */
    public boolean isVerifiedWithin(LocalDateTime now, Duration ttl) {
        return verified && verifiedAt != null && now.isBefore(verifiedAt.plus(ttl));
    }
}
