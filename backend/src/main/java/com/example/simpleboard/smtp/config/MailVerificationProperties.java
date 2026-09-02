package com.example.simpleboard.smtp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail")
public class MailVerificationProperties {

    /** false 면 인증번호를 메일로 보내지 않고 로그에만 찍는다. 개발 편의용이다. */
    private boolean enabled = false;

    /** 보내는 사람 주소. 보통 spring.mail.username 과 같다. */
    private String from = "noreply@simpleboard.local";

    /** 인증번호 유효 시간. */
    private Duration codeTtl = Duration.ofMinutes(5);

    /** 인증을 마친 뒤 회원가입까지 허용하는 시간. */
    private Duration verifiedTtl = Duration.ofMinutes(30);

    /** 재발송을 막는 최소 간격. */
    private Duration resendCooldown = Duration.ofSeconds(30);

    /** 인증번호를 틀릴 수 있는 횟수. */
    private int maxAttempts = 5;
}
